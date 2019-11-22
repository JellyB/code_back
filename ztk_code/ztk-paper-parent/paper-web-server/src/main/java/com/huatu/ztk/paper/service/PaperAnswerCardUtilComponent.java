package com.huatu.ztk.paper.service;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.daoPandora.EstimateMapper;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static service.impl.BaseServiceHelperImpl.throwBizException;


/**
 * Created by lijun on 2018/10/18
 */
@Component
public class PaperAnswerCardUtilComponent {

    private static final Logger logger = LoggerFactory.getLogger(PaperAnswerCardUtilComponent.class);
    //控制是否显示活动说明
    public static final int FALSE_STATUE = 0;

    public static final int TRUE_STATUE = 1;

    @Autowired
    private EstimateMapper estimateMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CourseConfig courseConfig;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private BigBagUsedSubjectConfig bigBagUsedSubjectConfig;

    /**
     * 活动信息 防止缓存穿透
     */
    private final static Cache<Integer, Boolean> ACTIVITY_NULL_HOLD = CacheBuilder.newBuilder()
            .expireAfterWrite(3 * 60, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();


    /**
     * 处理 精准估分 中 携带活动信息
     */
    public AnswerCard addGiftInfoForEstimateAnswerCard(AnswerCard answerCard) throws BizException {

        logger.info("科目是:{},答题卡类型是：{}", answerCard.getCatgory(), answerCard.getType());
        //检验答题卡信息
        if (!(answerCard instanceof StandardCard)) {
            return answerCard;
        }
        StandardCard standardCard = (StandardCard) answerCard;

        //模考大赛开关是否开启
        if (!judgeMatchIsOpen(answerCard.getType())) {
            return answerCard;
        }

        //添加公基科目精准估分
        if (bigBagUsedSubjectConfig.isEnabledUserSubject(answerCard.getSubject()) == false ||
                null == ((StandardCard) answerCard).getPaper()) {
            return answerCard;
        }

        if (AnswerCardType.ESTIMATE != standardCard.getType() && AnswerCardType.MATCH != standardCard.getType()) {
            //非 精准估分,模考大赛
            return answerCard;
        }

        AnswerCardWithGift answerCardWithGift = new AnswerCardWithGift();
        BeanUtils.copyProperties(answerCard, answerCardWithGift);

        // 获取对应的活动-礼包配置信息,
        Estimate estimate = getEstimateGiftInfoHash(((StandardCard) answerCard).getPaper().getId());
        if (null == estimate) {
            return answerCardWithGift;
        }

        String token = userSessionService.getTokenById(answerCard.getUserId());
        String userName = userSessionService.getUname(token);

        String giftHtmlUrl = null;
        int courseId;
        int type = 0;

        // 处理是否领取过,组装活动信息
        if (answerCardWithGift.getScore() >= estimate.getScore()) {
            answerCardWithGift = dealAddGroupUrl(estimate, answerCard.getUserId(), userName, answerCardWithGift);
            giftHtmlUrl = estimate.getUpGiftHtmlUrl();
            courseId = estimate.getUpCourseId();
            type = FALSE_STATUE;
        } else {
            answerCardWithGift = dealAddGroupUrl(estimate, answerCard.getUserId(), userName, answerCardWithGift);
            giftHtmlUrl = estimate.getDownGiftHtmlUrl();
            courseId = estimate.getDownCourseId();
            type = TRUE_STATUE;
        }
        //组装H5连接信息
        giftHtmlUrl = buildGiftHtmlUrlStr(giftHtmlUrl, answerCardWithGift.getHasGetBigGift(),
                token, courseId, estimate.getActivityId());
        answerCardWithGift.setGiftHtmlUrl(giftHtmlUrl);
        answerCardWithGift.setAddGroupUrl(addGroupUrlBuilder(answerCardWithGift.getAddGroupUrl(), token, type));
        answerCardWithGift.setHasGift(TRUE_STATUE);
        answerCardWithGift.setRightImgUrl(estimate.getRightImgUrl());
        answerCardWithGift.setGiftImgUrl(estimate.getGiftImgUrl());
        return answerCardWithGift;
    }


    /**
     * 处理是否已经领取过,返回不同的领取状态和显示图片
     */
    public AnswerCardWithGift dealAddGroupUrl(Estimate estimate, Long userId, String userName, AnswerCardWithGift answerCardWithGift) throws BizException {
        if (null == answerCardWithGift || null == estimate) {
            return answerCardWithGift;
        }

        Set<Integer> courseSet = new HashSet<>();
        courseSet.add(estimate.getDownCourseId());
        courseSet.add(estimate.getUpCourseId());

        Integer isHasGet = judgeIsHasGetGiftBag(courseSet, userId, userName, answerCardWithGift.getPaper().getId());
        logger.info("isHasGet result is:{}", isHasGet);
        if (isHasGet == TRUE_STATUE) {
            answerCardWithGift.setAddGroupUrl(estimate.getHasGetBagUrl());
        } else {
            answerCardWithGift.setAddGroupUrl(estimate.getNotGetBagUrl());
        }
        answerCardWithGift.setHasGetBigGift(isHasGet);
        return answerCardWithGift;
    }

    /**
     * 是否已经领取过图片
     *
     * @param courseSet
     * @param userName
     * @return
     */
    public Integer judgeIsHasGetGiftBag(Set<Integer> courseSet, Long userId, String userName, int paperId) {

        int hasGet = 0;
        SetOperations setOperations = redisTemplate.opsForSet();
        for (Integer courseId : courseSet) {
            Boolean flag = setOperations.isMember(userCourse(courseId), userId.toString());
            logger.info("course is cache:{}", flag);
            if (flag) {
                hasGet = TRUE_STATUE;
                break;
            }
        }
        //缓存无,请求接口判断
        if (hasGet == FALSE_STATUE) {
            logger.info("I come from db");
            String courseIds = courseSet.stream().map(String::valueOf).collect(Collectors.joining(","));
            hasGet = isHasGet(userName, courseIds);
            if (hasGet == TRUE_STATUE) {
                //如果有一个领取过，userId放入缓存
                for (Integer courseId : courseSet) {
                    setOperations.add(userCourse(courseId), userId.toString());
                    redisTemplate.expire(userCourse(courseId), 1, TimeUnit.DAYS);
                }

            }

        }
        return hasGet;
    }

    /**
     * 是否领取课程
     *
     * @param userName
     * @param classId
     * @return
     */
    public Integer isHasGet(String userName, String classId) {
        Map<String, Object> mapData = null;
        String url = courseConfig.getCourseUrl() + "/c/v5/order/hasGetBigGiftOrder?userName=" + userName + "&classId=" + classId;
        int hasGet = 0;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseMsg<Object> responseMsgResponseEntity = restTemplate.getForObject(url, ResponseMsg.class);
            if (responseMsgResponseEntity.getCode() != 1000000) {
                logger.info("url is :{}", url);
                logger.info(" course fail :{}", responseMsgResponseEntity.getMsg());
                throwBizException(responseMsgResponseEntity.getMsg());
            }
            if (responseMsgResponseEntity != null) {
                mapData = (Map) responseMsgResponseEntity.getData();
            }
            Boolean isHash = false;
            String[] ids = classId.split(",");
            for (String id : ids) {
                isHash = (Boolean) mapData.get(id);
                if (isHash == true) {
                    hasGet = TRUE_STATUE;
                    break;
                }
            }
        } catch (Exception ex) {
            logger.info("course return fail", ex);

        }
        return hasGet;
    }


    /**
     * 处理 精准估分页面 是否需要在试卷数据中显示 活动图标
     * 非行测，非模考大赛（精准估分）,不显示
     */
    public List<EstimatePaper> addGiftInfoForEstimateSearchList(final List<EstimatePaper> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<EstimatePaper> estimatePaperList = list.stream()
                .map(estimatePaper -> {
                    Estimate estimate = getEstimateGiftInfoHash(estimatePaper.getId());
                    if (null != estimate) {
                        estimatePaper.setIconUrl(estimate.getIconUrl());
                    }
                    return estimatePaper;
                })
                .collect(Collectors.toList());
        return estimatePaperList;
    }


    /**
     * 模考大赛列表活动图标
     * 非行测，非模考大赛（精准估分，不显示）
     */
    public String addGiftInfoForMatchSearchList(Match match) {
        if (null != match) {
            //模考大赛开关
            if (!judgeMatchIsOpen(AnswerCardType.MATCH)) {
                logger.info("开关是否开放");
                return null;
            }
            Estimate estimate = getEstimateGiftInfoHash(match.getPaperId());
            if (null != estimate) {
                return estimate.getIconUrl();
            }

        }
        return null;
    }


    /**
     * 获取模考礼包说明
     *
     * @param
     * @return
     */
    public HashMap getEstimateIntroduce(int type) {
        HashMap map = new HashMap();
        map.put("iconUrl", EstimateGiftBag.ICON_URL);
        map.put("isShow", FALSE_STATUE);
        if (type == AnswerCardType.ESTIMATE) {
            map.put("bigBagRemind", EstimateGiftBag.ESTIMATE_BIG_BAG_REMIND);
            map.put("giftIntroduce", EstimateGiftBag.GIFT_INTRODUCE);
        } else if (type == AnswerCardType.MATCH) {
            map.put("bigBagRemind", EstimateGiftBag.MATCH_BIG_BAG_REMIND);
            map.put("giftIntroduce", EstimateGiftBag.MATCH_GIFT_INTRODUCE);
        }
        return map;
    }


    /**
     * 查询精准估分礼包配置信息
     *
     * @param id
     * @return
     */
    public Estimate getEstimateGiftInfoHash(int id) {
        Estimate estimate = null;
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        try {
            String estimates = hashOperations.get(buildEstimateGiftKey(id), id + "");
            if (StringUtils.isNotEmpty(estimates)) {
                return JsonUtil.toObject(estimates, Estimate.class);
            }
            if (null != ACTIVITY_NULL_HOLD.getIfPresent(id)) {
                return estimate;
            }
        } catch (Exception ex) {
            logger.info("礼包redis获取错误", ex);
            hashOperations.delete(buildEstimateGiftKey(id), String.valueOf(id));
        }
        Example example = new Example(Estimate.class);
        example.and().andEqualTo("activityId", id)
                .andEqualTo("bizStatus", TRUE_STATUE)
                .andEqualTo("status", TRUE_STATUE);
        List<Estimate> currentEstimates = estimateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(currentEstimates)) {
            List<Estimate> estimateList = getCurrentActivityInfo(currentEstimates);
            if (CollectionUtils.isNotEmpty(estimateList)) {
                estimate = estimateList.get(0);
                hashOperations.put(buildEstimateGiftKey(id), id + "", JsonUtil.toJson(estimate));
                redisTemplate.expire(buildEstimateGiftKey(id), 15, TimeUnit.MINUTES);
            } else {
                ACTIVITY_NULL_HOLD.put(id, true);
            }
        }
        return estimate;

    }

    /**
     * 控制在活动期间展示，活动时间结束，配置失效
     *
     * @param estimateList
     */
    public List<Estimate> getCurrentActivityInfo(List<Estimate> estimateList) {
        //超过活动时间，自动下线

        List<Estimate> list = new ArrayList<>();
        for (Estimate estimate : estimateList) {
            Long paperId = estimate.getActivityId();
            Paper paper = mongoTemplate.findById(paperId, Paper.class);
            if (paper instanceof EstimatePaper) {
                final long offlineTime = ((EstimatePaper) paper).getOfflineTime();
                long currentTime = System.currentTimeMillis();
                //活动结束时间>当前时间 || 状态是上线
                if (offlineTime >= currentTime && paper.getStatus() == PaperStatus.AUDIT_SUCCESS) {
                    list.add(estimate);
                }
            }

        }
        return list;
    }

    /**
     * 处理精准估分或模考大赛是否显示活动介绍和图标
     */
    public HashMap dealEstimateIsShow(int subject, int type) {
        HashMap map = getEstimateIntroduce(type);

        //模考大赛设置开关,默认是关闭off，开启是on
        if (!judgeMatchIsOpen(type)) {
            return map;
        }

        //添加公基科目精准估分
        if (bigBagUsedSubjectConfig.isEnabledUserSubject(subject) == false) {
            return map;
        }
        if (type != AnswerCardType.ESTIMATE && type != AnswerCardType.MATCH) {
            return map;
        }
        Set<String> paperIds = null;
        if (type == AnswerCardType.ESTIMATE) {
            paperIds = getEstimateIds(subject);
        } else if (type == AnswerCardType.MATCH) {
            paperIds = getMatchId(subject);
        }

        List<Integer> paperIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paperIds)) {
            //处理试卷是否配置活动信息
            paperIds.stream().forEach(id -> {
                Integer paperId = Integer.valueOf(id);
                Estimate estimate = getEstimateGiftInfoHash(paperId);
                if (null != estimate) {
                    paperIdList.add(paperId);
                }
            });
            // logger.info("配置是：{}", paperIds);
            if (CollectionUtils.isNotEmpty(paperIdList)) {
                map.put("isShow", TRUE_STATUE);
            }
        }
        return map;
    }


    /**
     * 缓存估分列表内容
     */
    public Set<String> getEstimateIds(int subject) {
        SetOperations setOperations = redisTemplate.opsForSet();
        Set members = setOperations.members(currentEstimateKey(subject));

        if (CollectionUtils.isEmpty(members)) {
            List<EstimatePaper> estimatePaperList = paperDao.findEstimatePaperList(subject, PaperType.ESTIMATE_PAPER);
            Set<String> collectIds = estimatePaperList.stream().map(estimatePaper -> String.valueOf(estimatePaper.getId())
            ).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(collectIds)) {
                setOperations.add(currentEstimateKey(subject), collectIds.toArray());
                redisTemplate.expire(currentEstimateKey(subject), 5, TimeUnit.MINUTES);
                return collectIds;
            }
        }
        return members;
    }


    /**
     * 缓存当前模考大赛ID
     *
     * @param subjectId
     * @return
     */
    public Set<String> getMatchId(Integer subjectId) {

        SetOperations setOperations = redisTemplate.opsForSet();
        Set members = setOperations.members(currentMatchIdKey(subjectId));

        if (CollectionUtils.isEmpty(members)) {
            List<Match> matches = matchDao.findUsefulMatch(subjectId);
            if (CollectionUtils.isNotEmpty(matches)) {
                members = matches.stream().filter(match -> match.getEssayPaperId() <= 0)
                        .map(match -> String.valueOf(match.getPaperId()))
                        .collect(Collectors.toSet());

                if (CollectionUtils.isNotEmpty(members)) {
                    //放入缓存
                    setOperations.add(currentMatchIdKey(subjectId), members.toArray());
                    redisTemplate.expire(currentMatchIdKey(subjectId), 5, TimeUnit.MINUTES);
                }
            }
        }
        return members;
    }

    public Boolean judgeMatchIsOpen(int type) {
        //模考大赛设置开关,默认是关闭off，开启是on
        if (type == AnswerCardType.MATCH) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = EstimateGiftBag.BIG_BAG_OPEN;
            Object result = valueOperations.get(key);
            if (null == result || result.equals("off")) {
                return false;
            }
            return true;
        }
        return true;
    }


    /**
     * 当前模考大赛key
     *
     * @param subjectId
     * @return
     */
    public static String currentMatchIdKey(Integer subjectId) {
        StringBuffer builder = new StringBuffer(64);
        return builder.append("current:match")
                .append(":").append(subjectId)
                .toString();
    }


    /**
     * 当前科目下的精准估分列表
     *
     * @param subject 科目ID
     * @return
     */
    public static String currentEstimateKey(Integer subject) {
        StringBuffer estimatePaperKey = new StringBuffer(128);
        return estimatePaperKey.append("current:estimate")
                .append(":").append(subject)
                .toString();
    }


    /**
     * 拼接H5连接
     *
     * @param giftHtmlUrl   配置的h5页面连接
     * @param hasGetBigGift 是否领取过
     * @param token         用户token信息
     * @param courseId      课程ID
     * @return
     */
    public String buildGiftHtmlUrlStr(String giftHtmlUrl, Integer hasGetBigGift, String token, Integer courseId, Long paperId) {

        StringBuffer builder = new StringBuffer(128);
        return builder.append(giftHtmlUrl.trim())
                .append("?token=").append(token)
                .append("&hasGetBigGift=").append(hasGetBigGift)
                .append("&courseId=").append(courseId)
                .append("&paperId=").append(paperId)
                .toString();
    }

    /**
     * 拼接加群连接
     *
     * @param addGroupUr 加群连接
     * @param token      token信息
     * @param type       type定义类型
     * @return
     */
    public String addGroupUrlBuilder(String addGroupUr, String token, int type) {
        StringBuffer builder = new StringBuffer();
        return builder.append(addGroupUr.trim())
                .append("?token=").append(token)
                .append("&type=").append(type)
                .toString();
    }

    /**
     * 缓存用户已经领取课程key
     *
     * @param
     * @return
     */
    public static String userCourse(Integer courseId) {
        StringBuffer builder = new StringBuffer(64);
        return builder.append("estimate:course")
                .append(":").append(courseId)
                .toString();
    }


    /**
     * 礼包配置key
     *
     * @param id
     * @return
     */
    public String buildEstimateGiftKey(int id) {
        StringBuffer stringBuffer = new StringBuffer(128);
        return stringBuffer.append("pandora-server.")
                .append("estimate")
                .append("_")
                .append(id + "")
                .toString();
    }


}