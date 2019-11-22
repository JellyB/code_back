package com.huatu.tiku.match.service.impl.v1.search;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.manual.pandora.EstimateMapper;
import com.huatu.tiku.match.enums.EstimateStatusEnum;
import com.huatu.tiku.match.service.v1.search.GiftPackageService;
import com.huatu.tiku.match.ztk.api.CourseFeignClient;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.EstimateGiftBag;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-16 下午3:23
 **/

@Slf4j
@Service
public class GiftPackageServiceImpl implements GiftPackageService {

    @Autowired
    private EstimateMapper estimateMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CourseFeignClient courseFeignClient;

    /**
     * 防大礼包mysql查询穿透的字段内容
     */
    private final static String ESTIMATE_NULL = "estimate_null";

    /**
     * 处理大礼包url信息
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    @Override
    public String obtainGiftIconUrl(int paperId) throws BizException {
        Estimate estimate = obtainEstimateInfoByCache(paperId);
        if (null == estimate) {
            return null;
        }
        if (null != estimate.getIconUrl()) {
            return estimate.getIconUrl();
        } else {
            return null;
        }
    }

    /**
     * 处理答题卡大礼包信息
     *
     * @param answerCard
     * @param userName
     * @throws BizException
     */
    @Override
    public AnswerCard buildGiftInfo4AnswerCard(AnswerCard answerCard, String userName, String token) throws BizException {
        //检验答题卡信息
        AnswerCardWithGift answerCardWithGift = new AnswerCardWithGift();
        BeanUtils.copyProperties(answerCard, answerCardWithGift);
        if (!(answerCard instanceof StandardCard)) {
            return answerCardWithGift;
        }
        StandardCard standardCard = (StandardCard) answerCard;
        if (answerCard.getCatgory() != SubjectType.GWY_XINGCE || null == ((StandardCard) answerCard).getPaper()) {
            return answerCardWithGift;
        }
        Estimate estimate = obtainEstimateInfoByCache(standardCard.getPaper().getId());
        if (null == estimate) {
            return answerCardWithGift;
        }
        String giftHtmlUrl;
        int courseId;
        int type;
        if (answerCardWithGift.getScore() >= estimate.getScore()) {
            answerCardWithGift = dealAddGroupUrl(estimate, answerCard.getUserId(), userName, answerCardWithGift);
            giftHtmlUrl = estimate.getUpGiftHtmlUrl();
            courseId = estimate.getUpCourseId();
            type = EstimateStatusEnum.FALSE_STATUS.getValue();
        } else {
            answerCardWithGift = dealAddGroupUrl(estimate, answerCard.getUserId(), userName, answerCardWithGift);
            giftHtmlUrl = estimate.getDownGiftHtmlUrl();
            courseId = estimate.getDownCourseId();
            type = EstimateStatusEnum.TRUE_STATUS.getValue();
        }
        //组装H5连接信息
        giftHtmlUrl = buildGiftHtmlUrlStr(giftHtmlUrl, answerCardWithGift.getHasGetBigGift(),
                token, courseId);
        answerCardWithGift.setGiftHtmlUrl(giftHtmlUrl);
        answerCardWithGift.setAddGroupUrl(addGroupUrlBuilder(answerCardWithGift.getAddGroupUrl(), token, type));
        answerCardWithGift.setHasGift(EstimateStatusEnum.TRUE_STATUS.getValue());
        answerCardWithGift.setRightImgUrl(estimate.getRightImgUrl());
        answerCardWithGift.setGiftImgUrl(estimate.getGiftImgUrl());
        return answerCardWithGift;
    }


    /**
     * 缓存中获取礼包信息
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    private Estimate obtainEstimateInfoByCache(int paperId) throws BizException {

        //模考大赛开关是否开启
        if (!judgeMatchIsOpen(AnswerCardType.MATCH)) {
            return null;
        }

        String key = MatchInfoRedisKeys.getPaperGiftPackageInfo(paperId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object temp = valueOperations.get(key);
        if (null != temp) {
            String value = String.valueOf(temp);
            if (ESTIMATE_NULL.equals(value)) {
                return null;
            }
            return JSONObject.parseObject(value, Estimate.class);
        } else {
            Example example = new Example(Estimate.class);
            example.and().andEqualTo("activityId", paperId)
                    .andEqualTo("bizStatus", 1)
                    .andEqualTo("status", 1);
            example.orderBy("gmtCreate").desc();
            List<Estimate> currentEstimates = estimateMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(currentEstimates)) {
                Estimate estimate = currentEstimates.get(0);
                valueOperations.set(key, JSONObject.toJSONString(estimate), 7, TimeUnit.DAYS);
                return estimate;
            } else {
                valueOperations.set(key, ESTIMATE_NULL, 1, TimeUnit.MINUTES);
                return null;
            }
        }
    }


    /**
     * 处理是否已经领取过,返回不同的领取状态和显示图片
     */
    public AnswerCardWithGift dealAddGroupUrl(Estimate estimate, Long userId, String userName, AnswerCardWithGift answerCardWithGift) throws BizException {
        HashSet<Integer> courseSet = Sets.newHashSet();
        courseSet.add(estimate.getDownCourseId());
        courseSet.add(estimate.getUpCourseId());

        Boolean flag = judgeIsHasGetGiftBag(estimate.getDownCourseId(), userId, userName) ||
                judgeIsHasGetGiftBag(estimate.getUpCourseId(), userId, userName);

        if (flag) {
            answerCardWithGift.setAddGroupUrl(estimate.getHasGetBagUrl());
        } else {
            answerCardWithGift.setAddGroupUrl(estimate.getNotGetBagUrl());
        }
        answerCardWithGift.setHasGetBigGift(flag ? EstimateStatusEnum.TRUE_STATUS.getValue() : EstimateStatusEnum.FALSE_STATUS.getValue());
        return answerCardWithGift;
    }

    /**
     * 是否已经领取过图片
     *
     * @param courseId
     * @param userName
     * @return
     */
    public Boolean judgeIsHasGetGiftBag(Integer courseId, Long userId, String userName) throws BizException {
        SetOperations setOperations = redisTemplate.opsForSet();
        String key = MatchInfoRedisKeys.getGiftPackageCourseKey(String.valueOf(courseId));
        if (redisTemplate.hasKey(key)) {
            return setOperations.isMember(key, userName);
        } else {
            Map<String, Boolean> result = checkCurrentClassHasReceived(userName, String.valueOf(courseId));
            if (CollectionUtils.isEmpty(result.keySet())) {
                return false;
            }
            result.keySet().forEach(item -> {
                if (result.get(item)) {
                    setOperations.add(key, userId.toString());
                    redisTemplate.expire(key, 1, TimeUnit.DAYS);
                }
            });
            return result.get(String.valueOf(courseId));
        }
    }

    /**
     * 查询精准估分礼包配置信息
     *
     * @param paperId
     * @return
     */
    public Estimate obtainEstimateGiftInfoHash(int paperId) {
        Estimate estimate = null;
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String estimates = hashOperations.get(buildEstimateGiftKey(paperId), paperId + "");
        if (StringUtils.isNotEmpty(estimates)) {
            return JsonUtil.toObject(estimates, Estimate.class);
        }
        Example example = new Example(Estimate.class);
        example.and().andEqualTo("activityId", paperId)
                .andEqualTo("bizStatus", EstimateStatusEnum.TRUE_STATUS.getValue())
                .andEqualTo("status", EstimateStatusEnum.TRUE_STATUS.getValue());
        List<Estimate> currentEstimates = estimateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(currentEstimates)) {
            List<Estimate> estimateList = getCurrentActivityInfo(currentEstimates);
            if (CollectionUtils.isNotEmpty(estimateList)) {
                estimate = estimateList.get(0);
                hashOperations.put(buildEstimateGiftKey(paperId), paperId + "", JsonUtil.toJson(estimate));
                redisTemplate.expire(buildEstimateGiftKey(paperId), 15, TimeUnit.MINUTES);
            }
        }
        return estimate;

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
    public String buildGiftHtmlUrlStr(String giftHtmlUrl, Integer hasGetBigGift, String token, Integer courseId) {

        StringBuffer builder = new StringBuffer(128);
        return builder.append(giftHtmlUrl.trim())
                .append("?token=").append(token)
                .append("&hasGetBigGift=").append(hasGetBigGift)
                .append("&courseId=").append(courseId)
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
     * 判断模考大赛是否开启
     *
     * @param type
     * @return
     */
    public Boolean judgeMatchIsOpen(int type) {
        //模考大赛设置开关,默认是关闭off，开启是on
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        String key = EstimateGiftBag.BIG_BAG_OPEN;
        try {
            byte[] bytes = connection.get(key.getBytes());
            if (null != bytes) {
                String result = new String(bytes, "UTF-8");
                log.info("result是:{}", result);
                if (result.equals("on")) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            connection.close();
        }
        return false;
    }


    /**
     * 是否领取过课程
     * //todo test
     *
     * @param userName
     * @param classIds
     * @return
     * @throws BizException
     */
    @Override
    public Map<String, Boolean> checkCurrentClassHasReceived(String userName, String classIds) throws BizException {
        Map<String, Boolean> result = Maps.newHashMap();
        HashMap<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("userName", userName)
                .put("classId", classIds)
                .build();
        try {
            ResponseMsg<Object> responseMsgResponseEntity = courseFeignClient.isHasGet(params);
            if (null == responseMsgResponseEntity || null == responseMsgResponseEntity.getData()) {
                return result;
            }
            LinkedHashMap<String, Boolean> tmp = (LinkedHashMap<String, Boolean>) responseMsgResponseEntity.getData();
            result.putAll(tmp);
            return result;
        } catch (Exception ex) {
            log.info("course return fail", ex);
        }
        return result;
    }
}
