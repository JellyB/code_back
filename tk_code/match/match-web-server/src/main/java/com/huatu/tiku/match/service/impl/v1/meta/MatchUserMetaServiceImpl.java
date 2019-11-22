package com.huatu.tiku.match.service.impl.v1.meta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeBasedTable;
import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.common.bean.AreaConstants;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.constant.MatchErrors;
import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.dao.manual.meta.MatchUserMetaMapper;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.match.listener.enums.RabbitMatchKeyEnum;
import com.huatu.tiku.match.manager.MatchManager;
import com.huatu.tiku.match.service.impl.v1.paper.AnswerCardUtil;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.tiku.match.util.ConvertUtil;
import com.huatu.tiku.match.web.event.MatchUserChangeEvent;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import  com.huatu.common.exception.BizException;


/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Slf4j
@Service
public class MatchUserMetaServiceImpl extends BaseServiceHelperImpl<MatchUserMeta> implements MatchUserMetaService, ApplicationContextAware, ApplicationListener<MatchUserChangeEvent> {
    public MatchUserMetaServiceImpl() {
        super(MatchUserMeta.class);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate redisTemplateWithoutServerName;

    @Autowired
    MatchUserMetaMapper matchUserMetaMapper;

    @Autowired
    private MatchManager matchManager;

    @Autowired
    private PaperService paperService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${spring.profiles}")
    public String env;

    /**
     * 模考大赛最终报名人数缓存（考试开始后31分钟后的报名人数存入）
     */
    Cache<Integer, Integer> ENROLL_COUNT_FINAL_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .initialCapacity(10)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build();

    /**
     * 模考大赛是否已结束标识本地缓存
     */
    Cache<Integer, String> MATCH_FINISH_FLAG_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .initialCapacity(10)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    /**
     * @param userId
     * @param paperId
     * @return 要么返回正常值，要么返回null
     */
    @Override
    public MatchUserMeta findMatchUserEnrollInfo(int userId, int paperId) {
        //查询报名数据通过缓存
        MatchUserMeta matchUserMeta = null;
        String userEnrollKey = MatchInfoRedisKeys.getUserEnrollHashKey(paperId, userId);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> enrollMap = hashOperations.entries(userEnrollKey);
        if (MapUtils.isNotEmpty(enrollMap)) {
            if (MatchUserMetaUtil.IS_OTHER.equals(enrollMap.get(MatchUserMetaUtil.MetaAttrEnum.OTHER.getKey()))) {
                return null;
            } else {
                try {
                    matchUserMeta = MatchUserMetaUtil.convertEnrollMap2Object(enrollMap);
                    matchUserMeta.setMatchId(paperId);
                    matchUserMeta.setUserId(userId);
                    return matchUserMeta;
                } catch (Exception e) {
                    //缓存清空重新写入
                    redisTemplate.delete(userEnrollKey);
                    log.error("json parse error,enrollInfo = {}", enrollMap);
                    e.printStackTrace();
                }
            }
        }
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId).andEqualTo("userId", userId);
        List<MatchUserMeta> matchUserMetas = selectByExample(example);
        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
            matchUserMeta = matchUserMetas.get(0);
            //添加考试信息到缓存
            addEnrollInfo2Cache(matchUserMeta);
            //更新用户报名地区/学校到缓存
            saveEnroll2Cache(matchUserMeta);
        } else {
            addNullEnrollInfoCache(userId, paperId);
        }
        return matchUserMeta;

    }

    /**
     * 空报名信息缓存添加（防穿透）
     *
     * @param userId
     * @param paperId
     */
    private void addNullEnrollInfoCache(int userId, int paperId) {
        String userEnrollKey = MatchInfoRedisKeys.getUserEnrollHashKey(paperId, userId);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(userEnrollKey, MatchUserMetaUtil.MetaAttrEnum.OTHER.getKey(), MatchUserMetaUtil.IS_OTHER);
        redisTemplate.expire(userEnrollKey, 1, TimeUnit.MINUTES);
    }

    /**
     * 更新自身涉及到的报名数据(报名地区/学校)缓存
     * 缓存形式（用户ID---报名地区/学校）
     *
     * @param matchUserMeta
     */
    private void saveEnroll2Cache(MatchUserMeta matchUserMeta) {
        if (null == matchUserMeta) {
            return;
        }
        savePositionEnrollHash(matchUserMeta.getMatchId(), matchUserMeta.getUserId(), matchUserMeta.getPositionId());
        if (hasSchoolInfo(matchUserMeta)) {
            saveSchoolEnrollHash(matchUserMeta.getMatchId(), matchUserMeta.getUserId(), matchUserMeta.getSchoolId());
        }
    }

    private void saveSchoolEnrollHash(Integer matchId, Integer userId, Integer schoolId) {
        String schoolEnrollHashKey = MatchInfoRedisKeys.getMatchSchoolEnrollHashKey(matchId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(schoolEnrollHashKey, userId + "", schoolId + "");
        redisTemplate.expire(schoolEnrollHashKey, 15, TimeUnit.DAYS);
    }

    private void savePositionEnrollHash(int matchId, int userId, long positionId) {
        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(matchId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(positionEnrollHashKey, userId + "", positionId + "");
        redisTemplate.expire(positionEnrollHashKey, 15, TimeUnit.DAYS);
    }

    private void savePositionEnrollHashForSync(int matchId, int userId, long positionId) {
        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(matchId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(positionEnrollHashKey);
        if(MapUtils.isEmpty(entries)){          //无新模考数据则不做同步
            return;
        }
        String value = entries.get(userId + "");        //有新模考数据（或者新模考数据未过期），则添加导入的数据
        if(StringUtils.isBlank(value)){
            hashOperations.put(positionEnrollHashKey, userId + "", positionId + "");
            redisTemplate.expire(positionEnrollHashKey, 15, TimeUnit.DAYS);
        }
    }

    private boolean hasSchoolInfo(MatchUserMeta matchUserMeta) {
        Integer schoolId = matchUserMeta.getSchoolId();
        return null != schoolId && schoolId > 0;
    }


    /**
     * 报名数据写入逻辑
     *
     * @param userId
     * @param paperId
     * @param positionId
     * @param schoolId
     * @param schoolName
     * @param enrollTime
     * @return
     */
    @Override
    public int saveMatchEnrollInfo(int userId, int paperId, int positionId, int schoolId, String schoolName, long enrollTime, long essayPaperId) {

        boolean fromFlag = enrollTime == -1;    //是否通过新系统报名（如果是，则同步到旧系统，否则只添加，不同步）
        long current = fromFlag ? System.currentTimeMillis() : enrollTime;  //报名时间(新系统报名使用当前时间，老系统报名使用传入的时间)
        //TODO 报名地区名称通过文件加载得到，而非数据库查询得到
        String positionName = "全国";
        try {
            positionName = AreaConstants.getFullAreaNmae(positionId);
        } catch (Exception e) {
            log.error("position parse error,positionId = {}", positionId);
            positionId = -9;
        }
        //加入统计表
        MatchUserMeta matchUserMeta = MatchUserMeta.builder()
                .matchId(paperId)
                .userId(userId)
                .positionId(positionId)
                .positionName(positionName)
                .schoolId(schoolId)
                .schoolName(schoolName)
                .enrollTime(new Timestamp(current))
//                .essayPaperId(essayPaperId)
                .isAnswer(MatchInfoEnum.AnswerStatus.NO_JOIN.getKey())
                .build();
        //加入缓存
        int i = putEnrollInfo2Cache(matchUserMeta);
        /**
         * 维护报名数据到mysql
         */
        boolean isNewCreate = i > 0; //i标识被存储的报名信息量（1标识添加0标识修改）
        /**
         * 写入数据库
         */
        MatchUserChangeEvent.OperationEnum operationEnum = isNewCreate ? MatchUserChangeEvent.OperationEnum.INSERT : MatchUserChangeEvent.OperationEnum.UPDATE;
        MatchUserChangeEvent matchUserChangeEvent = new MatchUserChangeEvent(applicationContext, matchUserMeta, operationEnum);
        applicationContext.publishEvent(matchUserChangeEvent);
        /**
         * 缓存报名数据
         */
        addEnrollInfo2Cache(matchUserMeta);
        return i;
    }

    @Override
    public int getEnrollTotal(int paperId) {
        Integer total = ENROLL_COUNT_FINAL_CACHE.getIfPresent(paperId);
        if (null != total && total > 0) {
            return total;
        }
        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(paperId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Long size = hashOperations.size(positionEnrollHashKey);
        Paper paper = paperService.findPaperCacheById(paperId);
        BiConsumer<Paper, Integer> consumer = ((paperInfo, count) -> {      //缓存写入策略
            if (paperInfo instanceof EstimatePaper) {
                long startTime = ((EstimatePaper) paperInfo).getStartTime();
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis > startTime + TimeUnit.MINUTES.toMillis(31)) {
                    ENROLL_COUNT_FINAL_CACHE.put(paperInfo.getId(), count);
                }
            }
        });
        if (null != size && size > 0) {
            consumer.accept(paper, size.intValue());
            return size.intValue();
        }
        int count = getEnrollTotalBySql(paperId);
        consumer.accept(paper, size.intValue());
        return count;
    }

    /**
     * 查询报名总人数
     *
     * @param paperId
     * @return
     */
    private int getEnrollTotalBySql(int paperId) {
        /**
         * 报名总人数KEY/VALUE
         */
        String totalEnrollCountKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        try {
            String total = valueOperations.get(totalEnrollCountKey);
            if (null != total) {
                return Integer.parseInt(total);
            }
        } catch (Exception e) {
            e.printStackTrace();
            redisTemplate.delete(totalEnrollCountKey);
        }
        /**
         * 通过查询mysql表得到数据
         */
        try {
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", paperId);
            int i = matchUserMetaMapper.selectCountByExample(example);
            valueOperations.set(totalEnrollCountKey, i + "");
            redisTemplate.expire(totalEnrollCountKey, 1, TimeUnit.DAYS);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getPositionTotal(int paperId, int positionId) {
        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(paperId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> enrollMap = hashOperations.entries(positionEnrollHashKey);
        if (null == enrollMap) {
            return getPositionTotalByMysql(paperId, positionId);
        }
        Set<Map.Entry<String, String>> entries = enrollMap.entrySet();
        if (CollectionUtils.isNotEmpty(entries)) {
            long count = entries.stream().map(entry -> entry.getValue()).map(Integer::parseInt).filter(i -> i.equals(positionId)).count();
            return new Long(count).intValue();
        }
        return 0;
    }

    /**
     * 创建答题卡信息保存
     *
     * @param paperId
     * @param userId
     * @param practiceId
     * @param createTime
     * @return
     * @throws BizException
     */
    @Override
    public int savePracticeId(int paperId, int userId, long practiceId, long createTime) throws BizException {
        boolean fromFlag = createTime == -1; //是否通过新系统创建的答题卡（如果是，则同步到旧系统，否则只添加，不同步）
        long current = fromFlag ? System.currentTimeMillis() : createTime;
        MatchUserMeta matchUserEnrollInfo = findMatchUserEnrollInfo(userId, paperId);
        if (null == matchUserEnrollInfo) {
            PaperErrorInfo.AnswerCard.USER_NOT_ENROLL.exception();
            ;
        }
        /**
         * 缓存维护
         */
        String userEnrollHashKey = MatchInfoRedisKeys.getUserEnrollHashKey(paperId, userId);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map = Maps.newHashMap();
        map.put(MatchUserMetaUtil.MetaAttrEnum.PRACTICE_ID.getKey(), practiceId);
        map.put(MatchUserMetaUtil.MetaAttrEnum.CARD_CREATE_TIME.getKey(), current);
        map.put(MatchUserMetaUtil.MetaAttrEnum.IS_ANSWER.getKey(), MatchInfoEnum.AnswerStatus.NO_SUBMIT);
        map.put(MatchUserMetaUtil.MetaAttrEnum.OTHER.getKey(), MatchUserMetaUtil.IS_NOT_OTHER);        //缓存空值标识去除
        hashOperations.putAll(userEnrollHashKey, map);
        redisTemplate.expire(userEnrollHashKey, 15, TimeUnit.DAYS);
        /**
         * 创建答题卡ID和时间存储
         */
        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(paperId);
        SetOperations setOperations = redisTemplateWithoutServerName.opsForSet();
        setOperations.add(matchPracticeIdSetKey, practiceId + "");
        //TODO 通过事件队列实现
        MatchUserMeta matchUserMeta = MatchUserMeta.builder()
                .matchId(paperId)
                .userId(userId)
                .practiceId(practiceId)
                .cardCreateTime(new Timestamp(current))
                .isAnswer(MatchInfoEnum.AnswerStatus.NO_SUBMIT.getKey())
                .build();
        MatchUserChangeEvent matchUserChangeEvent = new MatchUserChangeEvent(applicationContext, matchUserMeta, MatchUserChangeEvent.OperationEnum.UPDATE);
        applicationContext.publishEvent(matchUserChangeEvent);
        return 1;
    }


    /**
     * 通过报名数据缓存获取答题卡ID（首页查询用户是否创建答题卡用到）
     *
     * @param paperId
     * @param userId
     * @return
     */
    @Override
    public long getMatchPracticeId(int paperId, int userId) {
        String enrollHashKey = MatchInfoRedisKeys.getUserEnrollHashKey(paperId, userId);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Object practiceObj = hashOperations.get(enrollHashKey, MatchUserMetaUtil.MetaAttrEnum.PRACTICE_ID.getKey());
        if (null == practiceObj) {
            return -1;
        }
        Long practiceId = Long.valueOf(String.valueOf(practiceObj));
        return practiceId > 0 ? practiceId : -1;
    }

    /**
     * 根据答题卡缓存+分数缓存查询考试分数是否存在
     *
     * @param paperId
     * @param userId
     * @return
     */
    @Override
    public boolean isExistedScore(int paperId, int userId) {
        long matchPracticeId = getMatchPracticeId(paperId, userId);
        if (matchPracticeId > 0) {
            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            Double score = zSetOperations.score(paperPracticeIdSore, String.valueOf(matchPracticeId));
            if (null != score) {
                return true;
            }
        }
        return false;
    }


    /**
     * 考试分数存储
     *
     * @param paperId
     * @param userId
     * @param submitTypeEnum
     * @param score
     * @param submitTime
     * @return
     * @throws BizException
     */
    @Override
    public int saveMatchScore(int paperId, int userId, MatchInfoEnum.SubmitTypeEnum submitTypeEnum, double score, long submitTime) throws BizException {
        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(paperId);
        SetOperations setOperations = redisTemplateWithoutServerName.opsForSet();
        /**
         * 删除用户答题卡预存zset
         */
        MatchUserMeta matchUserEnrollInfo = findMatchUserEnrollInfo(userId, paperId);
        if (null == matchUserEnrollInfo) {
            PaperErrorInfo.AnswerCard.USER_NOT_ENROLL.exception();
        }
        Long practiceId = matchUserEnrollInfo.getPracticeId();
        //添加排名信息(排名备选数据)
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(paperPracticeIdSore, String.valueOf(practiceId), score);
        redisTemplate.expire(paperPracticeIdSore,1,TimeUnit.DAYS);
        if (null == practiceId || practiceId < 0) {
            throw new com.huatu.common.exception.BizException(MatchErrors.NO_PRACTICEID);
        }
        setOperations.remove(matchPracticeIdSetKey, practiceId + "");
        boolean fromFlag = submitTime == -1; //提交答题卡时间（如果是，则同步到旧系统，否则只添加，不同步）
        long current = fromFlag ? System.currentTimeMillis() : submitTime;
        /**
         * 缓存维护
         */
        String userEnrollHashKey = MatchInfoRedisKeys.getUserEnrollHashKey(paperId, userId);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map = Maps.newHashMap();
        map.put(MatchUserMetaUtil.MetaAttrEnum.SCORE.getKey(), score);
        map.put(MatchUserMetaUtil.MetaAttrEnum.SUBMIT_TYPE.getKey(), submitTypeEnum.getKey());
        map.put(MatchUserMetaUtil.MetaAttrEnum.SUBMIT_TIME.getKey(), current);
        map.put(MatchUserMetaUtil.MetaAttrEnum.IS_ANSWER.getKey(), MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
        map.put(MatchUserMetaUtil.MetaAttrEnum.OTHER.getKey(), MatchUserMetaUtil.IS_NOT_OTHER);        //缓存空值标识去除
        hashOperations.putAll(userEnrollHashKey, map);
        redisTemplate.expire(userEnrollHashKey, 15, TimeUnit.DAYS);
        //TODO 通过事件队列实现
        MatchUserMeta matchUserMeta = MatchUserMeta.builder()
                .matchId(paperId)
                .userId(userId)
                .score(score)
                .submitTime(new Timestamp(current))
                .submitType(submitTypeEnum.getKey())
                .isAnswer(MatchInfoEnum.AnswerStatus.SUBMIT.getKey())
                .build();
        MatchUserChangeEvent matchUserChangeEvent = new MatchUserChangeEvent(applicationContext, matchUserMeta, MatchUserChangeEvent.OperationEnum.UPDATE);
        applicationContext.publishEvent(matchUserChangeEvent);
        return 1;
    }


    /**
     * 是否提交试卷
     *
     * @param paperId
     * @param userId
     * @return
     */
    @Override
    public boolean isSubmitted(int paperId, int userId) {
        MatchUserMeta matchUserEnrollInfo = findMatchUserEnrollInfo(userId, paperId);
        Integer isAnswer = matchUserEnrollInfo.getIsAnswer();
        if (null != isAnswer && isAnswer.equals(MatchInfoEnum.AnswerStatus.SUBMIT.getKey())) {
            return true;
        }
        return false;
    }

    /**
     * 模考大赛答题卡批改完成
     *
     * @param paperId
     * @return
     */
    @Override
    public boolean isFinished(int paperId) {
        Paper paper = paperService.findPaperCacheById(paperId);
        long currentTimeMillis = System.currentTimeMillis();
        if (paper instanceof EstimatePaper) {
            long endTime = ((EstimatePaper) paper).getEndTime();
            if (endTime > currentTimeMillis) {
                return false;
            }
        }
        try {
            String expire_flag = MATCH_FINISH_FLAG_CACHE.getIfPresent(paperId);
            if (StringUtils.isNotBlank(expire_flag)) {
                String[] s = expire_flag.split("_");
                if (Long.parseLong(s[0]) > currentTimeMillis) {     //未过期
                    return "1".equals(s[1]) ? true : false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MATCH_FINISH_FLAG_CACHE.invalidate(paperId);
        }

        String matchFinishFlag = MatchInfoRedisKeys.getMatchFinishFlag(paperId);
        Object value = redisTemplate.opsForValue().get(matchFinishFlag);
        try {
            if (null != value) {
                int i = Integer.parseInt(value.toString());
                Long expire = redisTemplate.getExpire(matchFinishFlag, TimeUnit.MILLISECONDS);
                if (expire > 0) {
                    long newExpireTime = currentTimeMillis + expire;
                    MATCH_FINISH_FLAG_CACHE.put(paperId, newExpireTime + "_" + i);
                    return i == 1 ? true : false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(paperId);
        SetOperations setOperations = redisTemplateWithoutServerName.opsForSet();
        Long size = setOperations.size(matchPracticeIdSetKey);
        boolean flag = null != size && size > 0;        //未结束标识
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(matchFinishFlag, flag ? "0" : "1");
        if (ifAbsent) {
            redisTemplate.expire(matchFinishFlag, 1, TimeUnit.MINUTES);
            long l = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
            MATCH_FINISH_FLAG_CACHE.put(paperId, l + "_" + (flag ? "0" : "1"));
        }
        return !flag;
    }

    /**
     * 用户模考大赛成绩生成曲线图
     *
     * @param matchUserMetas 用户统计数据
     * @return
     */
    @Override
    public Line getMatchLine(List<MatchUserMeta> matchUserMetas) {
        final TreeBasedTable<Long, String, Number> basedTable = TreeBasedTable.create();
        for (MatchUserMeta matchUserMeta : matchUserMetas) {
            try {
                Integer paperId = matchUserMeta.getMatchId();
                Paper paper = paperService.findPaperCacheById(paperId);
                double totalScore = AnswerCardUtil.getAnswerTotalScore(paper);
                Double score = matchUserMeta.getScore();
                Double average = getAverage(matchUserMeta.getMatchId());
                Double score_ = new BigDecimal(score * 100 / totalScore).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                Double average_ = new BigDecimal(average * 100 / totalScore).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (score_ > 100) {
                    AnswerCard answerCard = answerCardDao.findById(matchUserMeta.getPracticeId());
                    if (answerCard instanceof StandardCard) {
                        Paper tempScore = ((StandardCard) answerCard).getPaper();
                        totalScore = AnswerCardUtil.getAnswerTotalScore(tempScore);
                        average = getAverage(matchUserMeta.getMatchId());
                        score_ = new BigDecimal(score * 100 / totalScore).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                        average_ = new BigDecimal(average * 100 / totalScore).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }
                basedTable.put(matchUserMeta.getSubmitTime().getTime(), "模考正确率", score_);
                basedTable.put(matchUserMeta.getSubmitTime().getTime(), "全站正确率", average_);
            } catch (Exception e) {
                log.error("曲线图节点有问题：paperId={},answerCardId={}", matchUserMeta.getMatchId(), matchUserMeta.getPracticeId());
                e.printStackTrace();
            }
        }
        return ConvertUtil.table2LineSeries(basedTable);
    }

    /**
     * 最大分数查询
     *
     * @param paperId
     * @return
     */
    @Override
    public Double getMaxScore(int paperId) {
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Long size = zSetOperations.size(paperPracticeIdSore);
        if (null != size && size > 0) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.reverseRangeWithScores(paperPracticeIdSore, 0, 0);

            if (CollectionUtils.isNotEmpty(withScores)) {
                return new ArrayList<>(withScores).get(0).getScore();
            }
        }
        return getMaxScoreBySql(paperId);

    }

    /**
     * 获取用户报告信息
     *
     * @param paperId
     * @param userId
     * @return
     */
    @Override
    public MatchUserMeta getReport(int paperId, int userId) throws BizException {
        /**
         * 模考统计信息表缓存数据查询
         */
        StopWatch stopWatch = new StopWatch("getReport");
        stopWatch.start("findMatchUserEnrollInfo");
        MatchUserMeta matchUserEnrollInfo = findMatchUserEnrollInfo(userId, paperId);
        //考试是否结束判断
        if (!isFinished(paperId)) {
             throwBizException(MatchErrors.UN_FINISHED.getMessage());
        }
        //是否交卷
        Timestamp submitTime = matchUserEnrollInfo.getSubmitTime();
        if (null == submitTime) {
            throw new BizException(MatchErrors.UN_SUBMITED);
        }
        //是否有成绩
        Double score = matchUserEnrollInfo.getScore();
        if (null == score) {
            throw new BizException(MatchErrors.NO_CORRECTED);
        }
        Boolean changeFlag = false; //matchUserEnrollInfo 是否做过补充
        Boolean changeFlag2DB = false; //持久化数据是否要做补充
        //最高分数作为考试整体数据，不在持久化数据中
        Double maxScore = matchUserEnrollInfo.getMaxScore();
        stopWatch.stop();
        if (null == maxScore || maxScore.intValue() < 0) {
            stopWatch.start("getMaxScore");
            matchUserEnrollInfo.setMaxScore(getMaxScore(paperId));
            stopWatch.stop();
            changeFlag = true;
        }
        //总排名人数作为考试整体数据，不在持久化数据中
        Integer rankCount = matchUserEnrollInfo.getRankCount();
        if (null == rankCount || rankCount.intValue() < 0) {
            stopWatch.start("getRankCount");
            matchUserEnrollInfo.setRankCount(getRankCount(paperId));
            changeFlag = true;
            stopWatch.stop();
        }
        //平均分作为考试整体数据，不在持久化数据中
        Double average = matchUserEnrollInfo.getAverage();
        if (null == average || average.intValue() < 0) {
            stopWatch.start("getAverage");
            matchUserEnrollInfo.setAverage(getAverage(paperId));
            changeFlag = true;
            stopWatch.stop();
        }
        //用户排名
        Integer rank = matchUserEnrollInfo.getRank();
        if (null == rank || rank.intValue() < 0) {
            stopWatch.start("getRank");
            matchUserEnrollInfo.setRank(getRank(paperId, userId, score));
            changeFlag = true;
            changeFlag2DB = true;
            stopWatch.stop();
        }
        //用户地区排名
        Integer rankForPosition = matchUserEnrollInfo.getRankForPosition();
        Integer positionId = matchUserEnrollInfo.getPositionId();
        if (null == rankForPosition || rankForPosition.intValue() < 0) {
            stopWatch.start("getRankForPosition");
            matchUserEnrollInfo.setRankForPosition(getRankForPosition(paperId, positionId, userId, score));
            changeFlag = true;
            changeFlag2DB = true;
            stopWatch.stop();
        }
        Integer rankCountForPosition = matchUserEnrollInfo.getRankCountForPosition();
        if (null == rankCountForPosition || rankCountForPosition.intValue() < 0) {
            stopWatch.start("getRankCountForPosition");
            matchUserEnrollInfo.setRankCountForPosition(getRankCountForPosition(paperId, positionId));
            changeFlag = true;
            stopWatch.stop();
        }
        if (changeFlag) {
            stopWatch.start("addEnrollInfo2Cache");
            addEnrollInfo2Cache(matchUserEnrollInfo);
            stopWatch.stop();
        }
        if (changeFlag2DB) {
            stopWatch.start("change2DB");
            MatchUserChangeEvent.OperationEnum operationEnum = MatchUserChangeEvent.OperationEnum.UPDATE;
            MatchUserChangeEvent matchUserChangeEvent = new MatchUserChangeEvent(applicationContext, matchUserEnrollInfo, operationEnum);
            applicationContext.publishEvent(matchUserChangeEvent);
            stopWatch.stop();
        }
        log.info("getReport stopWatch:{}", stopWatch.prettyPrint());
        return matchUserEnrollInfo;
    }

    @Override
    public void sync2DB(com.huatu.ztk.paper.bean.MatchUserMeta userMeta, AnswerCard answerCard, long essayPaperId) {
        long createTime = System.currentTimeMillis();
		try {
			if (answerCard != null) {
				createTime = answerCard.getCreateTime();
			}
		} catch (Exception e) {
            log.info(e.getMessage() + ":" + JsonUtil.toJson(userMeta));
        }
        System.out.println("createTime = " + createTime);
        MatchUserMeta matchUserMeta = MatchUserMeta.builder()
                .matchId(userMeta.getPaperId())
                .userId(new Long(userMeta.getUserId()).intValue())
                .positionId(userMeta.getPositionId())
                .positionName(userMeta.getPositionName())
                .enrollTime(new Timestamp(createTime))
                .isAnswer(MatchInfoEnum.AnswerStatus.NO_JOIN.getKey())
                .practiceId(userMeta.getPracticeId())
                .build();
        //根据情况将报名信息同步到hash缓存中，确保试卷的报名总数一起同步过来
        savePositionEnrollHashForSync(matchUserMeta.getMatchId(),matchUserMeta.getUserId(),matchUserMeta.getPositionId());
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", userMeta.getPaperId()).andEqualTo("userId", new Long(userMeta.getUserId()).intValue());
        List<MatchUserMeta> matchUserMetas = selectByExample(example);
        if (null != answerCard) {     //老逻辑可以查到报名数据和答题卡数据，则以老逻辑的数据覆盖新报名数据（先删后加）
            try {
                int deleteCount = matchUserMetaMapper.deleteByExample(example);
                log.info("删除数据：userId = {},matchId= {},meta= {}", userMeta.getUserId(), userMeta.getPaperId(), deleteCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CollectionUtils.isNotEmpty(matchUserMetas)) {   //老逻辑只有报名数据，且新逻辑已有统计数据，则不做同步，直接return(防止新逻辑产生的数据被老逻辑覆盖)
            return;
        }
        //此时，各种判断的结果都是需要重新从老逻辑同步数据到新逻辑
        if (null != userMeta.getSchoolId()) {
            matchUserMeta.setSchoolId(new Long(userMeta.getSchoolId()).intValue());
            matchUserMeta.setSchoolName(userMeta.getSchoolName());
        }
        if (null != answerCard) {
            matchUserMeta.setCardCreateTime(new Timestamp(createTime));
            matchUserMeta.setScore(answerCard.getScore());
            boolean doFlag = false;
            for (int i : answerCard.getCorrects()) {
                if (i != 0) {
                    doFlag = true;
                    break;
                }
            }
            matchUserMeta.setIsAnswer(MatchInfoEnum.AnswerStatus.NO_SUBMIT.getKey());
            if (doFlag) {
                matchUserMeta.setSubmitTime(new Timestamp(createTime));
                matchUserMeta.setSubmitType(MatchInfoEnum.SubmitTypeEnum.MANUAL_SUBMIT.getKey());
                matchUserMeta.setIsAnswer(MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
            } else if (answerCard.getStatus() == AnswerCardStatus.FINISH) {
                matchUserMeta.setSubmitTime(new Timestamp(createTime));
                matchUserMeta.setSubmitType(MatchInfoEnum.SubmitTypeEnum.AUTO_SUBMIT.getKey());
                matchUserMeta.setIsAnswer(MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
            }
        }
        save(matchUserMeta);
        /**
         * 删除用户统计缓存数据
         */
        String userEnrollKey = MatchInfoRedisKeys.getUserEnrollHashKey(matchUserMeta.getMatchId(), matchUserMeta.getUserId());
        redisTemplate.delete(userEnrollKey);
    }

    @Override
    public void restAnswerCard() {
        Consumer<MatchUserMeta> clear = (meta -> {
            System.out.println(meta.getId());
            if (null == meta) {
                return;
            }
            Long practiceId = meta.getPracticeId();
            System.out.println("syncQuestionMetaInfo , practiceId = " + practiceId);
            if (null == practiceId || practiceId <= 0) {
                return;
            }
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("practiceId", practiceId);
            rabbitTemplate.convertAndSend("", RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.MatchQuestionMetaSync, env), map);
        });

        //主体逻辑
        readAllAndOperation(clear);
    }

    private void readAllAndOperation(Consumer<MatchUserMeta> clear) {
        int index = 0;
        int limit = 1000;
        while (true) {
            List<MatchUserMeta> metas = findByCursor(index, limit);
            if (CollectionUtils.isEmpty(metas)) {
                break;
            }
            metas.parallelStream().forEach(clear::accept);
            Optional<Integer> max = metas.stream().map(BaseEntity::getId).map(Long::intValue).max(Integer::compare);
            if (max.isPresent()) {
                index = max.get();
            }
        }
    }

    private List<MatchUserMeta> findByCursor(int index, int limit) {
        List<HashMap> list = matchUserMetaMapper.findByCursor(index, limit);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(i -> {
            return MatchUserMeta.builder().id(MapUtils.getLong(i, "id"))
                    .matchId(MapUtils.getInteger(i, "match_id"))
                    .practiceId(MapUtils.getLong(i, "practice_id", -1L))
                    .build();
        }).collect(Collectors.toList());
    }


    /**
     * 查询用户完成的模考大赛统计信息
     *
     * @param userId
     * @param paperId
     * @return
     */
    private List<MatchUserMeta> list(int userId, int paperId) {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId)
                .andEqualTo("userId", userId)
                .andIsNotNull("submitTime");
        return selectByExample(example);
    }

    private Integer getRankCountForPosition(int paperId, Integer positionId) {
        String positionPracticeIdSore = MatchRedisKeys.getPositionPracticeIdSore(paperId, positionId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Long size = zSetOperations.size(positionPracticeIdSore);
        if (null != size && size > 0) {
            return size.intValue();
        }
        return getRankCountForPositionBySql(paperId, positionId);
    }

    /**
     * 基于mysql的地区参与人数统计
     *
     * @param paperId
     * @param positionId
     * @return
     */
    private Integer getRankCountForPositionBySql(int paperId, Integer positionId) {
        String rankCountForPositionHashKey = MatchInfoRedisKeys.getRankCountForPositionHashKey(paperId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        try {
            String value = hashOperations.get(rankCountForPositionHashKey, positionId + "");
            if (null != value) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            hashOperations.delete(rankCountForPositionHashKey, positionId + "");
        }
        //查询mysql
        try {
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", paperId)
                    .andEqualTo("positionId", positionId)
                    .andGreaterThan("practiceId", -1);
            int count = selectCountByExample(example);
            hashOperations.put(rankCountForPositionHashKey, positionId + "", count + "");
            redisTemplate.expire(rankCountForPositionHashKey, 30, TimeUnit.DAYS);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Integer getRankForPosition(int paperId, Integer positionId, int userId, Double score) {
        String positionPracticeIdSore = MatchRedisKeys.getPositionPracticeIdSore(paperId, positionId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Long size = zSetOperations.size(positionPracticeIdSore);
        if (null != size && size > 0) {
            long matchPracticeId = getMatchPracticeId(paperId, userId);
            Long rank = zSetOperations.reverseRank(positionPracticeIdSore, matchPracticeId + "");
            if (rank != null) {
                return rank.intValue() + 1;
            }
        }
        return getRankForPositionBySql(paperId, positionId, userId, score);

    }

    /**
     * zset过期后，基于mysql统计的数据
     *
     * @param paperId
     * @param positionId
     * @param userId
     * @param score
     * @return
     */
    private Integer getRankForPositionBySql(int paperId, int positionId, int userId, Double score) {
//        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchUserPositionRankHashKey(paperId, positionId);
//        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        try {
//            String rank = hashOperations.get(positionEnrollHashKey, userId + "");
//            if (null != rank) {
//                return Integer.parseInt(rank);
//            }
//        } catch (Exception e) {
//            hashOperations.delete(positionEnrollHashKey, userId + "");
//        }
        //查询mysql
        try {
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", paperId)
                    .andEqualTo("positionId", positionId)
                    .andGreaterThan("score", score);
            int count = selectCountByExample(example);
//            hashOperations.put(positionEnrollHashKey, userId + "", count + 1 + "");
//            redisTemplate.expire(positionEnrollHashKey, 365, TimeUnit.DAYS);
            return count + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Integer getRank(int paperId, int userId, double score) throws BizException {
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //本答题卡排名,redis rank命令,从0开始,也就是第一名的rank值为0
        long matchPracticeId = getMatchPracticeId(paperId, userId);
        if (matchPracticeId < 0) {
            throw new BizException(MatchErrors.NO_PRACTICEID);
        }
        Long rank = zSetOperations.reverseRank(paperPracticeIdSore, matchPracticeId + "");//排名,按照分数倒排
        if (rank != null) {//排名不存在
            return rank.intValue() + 1;
        }
        return getRankBySql(paperId, userId, score);
    }

    /**
     * 根据score获取排名(后期zset过期后的数据补充)
     *
     * @param paperId
     * @param userId
     * @param score
     * @return
     */
    private Integer getRankBySql(int paperId, int userId, double score) {
//        String matchUserRankHashKey = MatchInfoRedisKeys.getMatchUserRankHashKey(paperId);
//        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        try {
//            String rank = hashOperations.get(matchUserRankHashKey, userId + "");
//            if (null != rank) {
//                return Integer.parseInt(rank);
//            }
//        } catch (Exception e) {
//            hashOperations.delete(matchUserRankHashKey, userId + "");
//        }
        //查询mysql
        try {
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", paperId)
                    .andGreaterThan("score", score);
            int count = selectCountByExample(example);
//            hashOperations.put(matchUserRankHashKey, userId + "", (count + 1) + "");
//            redisTemplate.expire(matchUserRankHashKey, 365, TimeUnit.DAYS);
            return count + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Integer getRankCount(int paperId) {
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Long size = zSetOperations.size(paperPracticeIdSore);
        if (null != size && size > 0) {
            return size.intValue();
        }
        return getRankCountBySql(paperId);
    }

    /**
     * 基于持久化数据的查询
     *
     * @param paperId
     * @return
     */
    private Integer getRankCountBySql(int paperId) {
        String userSubmitCount = MatchInfoRedisKeys.getUserSubmitCount(paperId);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        try {
            String count = valueOperations.get(userSubmitCount);
            if (StringUtils.isNotBlank(count)) {
                return Integer.parseInt(count);
            }
        } catch (Exception e) {
            redisTemplate.delete(userSubmitCount);
        }
        //通过mysql查询参与总人数，并存入缓存
        try {
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", paperId)
                    .andGreaterThan("practiceId", -1)
                    .andEqualTo("isAnswer", MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
            int total = selectCountByExample(example);
            if (total > 0) {
                valueOperations.set(userSubmitCount, total + "");
                redisTemplate.expire(userSubmitCount, 365, TimeUnit.DAYS);
                return new Long(total).intValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 基于mysql统计表数据得到最大分数
     *
     * @param paperId
     * @return
     */
    private Double getMaxScoreBySql(int paperId) {
        String userMaxScoreKey = MatchInfoRedisKeys.getUserMaxScoreKey(paperId);
        ValueOperations<String, Double> valueOperations = redisTemplate.opsForValue();
        Double maxScore = valueOperations.get(userMaxScoreKey);
        if (null != maxScore) {
            return maxScore;
        }
        //通过mysql查询最大值，并存入缓存
        try {
            Double max = matchUserMetaMapper.findMatchUserMaxScore(paperId);
            if (null != max) {
                valueOperations.set(userMaxScoreKey, max);
                redisTemplate.expire(userMaxScoreKey, 365, TimeUnit.DAYS);
                return max;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0d;
    }

    /**
     * 查询考试平均分
     *
     * @param paperId
     * @return
     */
    private Double getAverage(int paperId) {
        Double sum = getMatchSumScore(paperId);
        int size = getRankCount(paperId);
        try {
            return sum / size;
        } catch (Exception e) {
            log.error("caculate error,sum = {},size={}", sum, size);
            e.printStackTrace();
        }
        return 0d;
    }

    /**
     * 考试累加成绩查询
     *
     * @param paperId
     * @return
     */
    private Double getMatchSumScore(int paperId) {
        String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId);
        ValueOperations<String, Double> valueOperations = redisTemplate.opsForValue();
        Double sum = valueOperations.get(paperScoreSum);
        if (null == sum || sum.intValue() == 0) {
            sum = getSumBySql(paperId);
            valueOperations.set(paperScoreSum, sum);
            redisTemplate.expire(paperScoreSum, 365, TimeUnit.DAYS);
        }
        return sum;
    }

    private Double getSumBySql(int paperId) {
        Double total = matchUserMetaMapper.findMatchUserSumScore(paperId);
        if (null != total) {
            return total;
        }
        return 0D;
    }


    /**
     * 获取有用的用户报告数据（模考历史及报告使用）
     *
     * @param userId
     * @param tagId
     * @param subjectId
     * @return
     */
    @Override
    public List<MatchUserMeta> getAvailableMatchMeta(int userId, int tagId, int subjectId) {
        StopWatch stopWatch = new StopWatch("getAvailableMatchMeta");
        stopWatch.start("findBySubjectAndTag");
        List<Match> matches = matchManager.findBySubjectAndTag(subjectId, tagId);
        stopWatch.stop();
        stopWatch.start("getMatchUserMetas");
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        long nowTime = System.currentTimeMillis();
        List<Integer> paperIds = matches.stream()
                .filter(i -> i.getEndTime() <= nowTime)                 //筛选已经结束的考试
                .map(Match::getPaperId).collect(Collectors.toList());
        List<MatchUserMeta> matchUserMetas = getMatchUserMetas(userId);
        stopWatch.stop();
        stopWatch.start("filter vailMatches");
        //判断是否参加过考试
        if (CollectionUtils.isEmpty(matchUserMetas)) {
            return Lists.newArrayList();
        }
        /**
         * 筛选参加过考试的并且已经结束的考试
         */
        List<MatchUserMeta> vailMatches = matchUserMetas.stream()
                .filter(i -> paperIds.contains(i.getMatchId()))
                .filter(i -> isFinished(i.getMatchId()))
                .filter(i -> null != i.getScore())
                .filter(i -> null != i.getSubmitTime() && i.getSubmitTime().getTime() < nowTime)
                .collect(Collectors.toList());
        stopWatch.stop();
        for (MatchUserMeta vailMatch : vailMatches) {
            Integer matchId = vailMatch.getMatchId();
            stopWatch.start("getRankCount:" + matchId);
            Optional<Match> first = matches.stream().filter(i -> i.getPaperId() == matchId.intValue())
                    .findFirst();
            if (first.isPresent()) {
                Match match = first.get();
                vailMatch.setName(match.getName());
                vailMatch.setSubjectId(match.getSubject());
                vailMatch.setTagId(match.getTag());
                if (null == vailMatch.getRankCount() || vailMatch.getRankCount() <= 0) {
                    vailMatch.setRankCount(getRankCount(matchId));
                }
            }
            stopWatch.stop();
        }
        vailMatches.sort(Comparator.comparing(MatchUserMeta::getSubmitTime));
        log.info("getAvailableMatchMeta stopWatch:{}", stopWatch.prettyPrint());
        return vailMatches;
    }

    /**
     * 获取用户所有行测统计数据
     *
     * @param userId
     */
    private List<MatchUserMeta> getMatchUserMetas(int userId) {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("userId", userId);
        List<MatchUserMeta> matchUserMetas = selectByExample(example);
        return matchUserMetas;
    }

    /**
     * 查询模考大赛地区报名人数
     *
     * @param paperId
     * @param positionId
     * @return
     */
    private int getPositionTotalByMysql(int paperId, int positionId) {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId);
        List<MatchUserMeta> matchUserMetas = selectByExample(example);
        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
            Map<Integer, List<MatchUserMeta>> positionMap = matchUserMetas.stream().collect(Collectors.groupingBy(MatchUserMeta::getPositionId));
            int size = positionMap.getOrDefault(new Integer(positionId), Lists.newArrayList()).size();
            addPositionEnrollInfo(paperId, positionMap);
            return size;
        }
        return 0;
    }

    /**
     * 同步所有报名数据
     *
     * @param paperId
     * @param positionMap
     */
    private void addPositionEnrollInfo(int paperId, Map<Integer, List<MatchUserMeta>> positionMap) {
        for (Map.Entry<Integer, List<MatchUserMeta>> entry : positionMap.entrySet()) {
            List<MatchUserMeta> matchUserMetas = entry.getValue();
            //缓存
            List<String> userIds = matchUserMetas.stream().map(MatchUserMeta::getUserId).map(String::valueOf)
                    .collect(Collectors.toList());
            addPositionEnrollInfo(paperId, entry.getKey(), userIds);
        }
    }

    /**
     * 异步添加某个地区报名相关数据
     *
     * @param paperId
     * @param positionId
     * @param userIds
     */
    private void addPositionEnrollInfo(int paperId, int positionId, List<String> userIds) {
        String positionEnrollHashKey = MatchInfoRedisKeys.getMatchPositionEnrollHashKey(paperId);
        Map<String, String> positionMap = userIds.stream().collect(Collectors.toMap(i -> i, i -> String.valueOf(positionId)));
        addPositionEnrollHashCache(positionEnrollHashKey, positionMap);
    }

    /**
     * 异步将持久化的地区报名userId，写入缓存
     *
     * @param positionEnrollHashKey
     * @param positionMap
     */
    private void addPositionEnrollHashCache(String positionEnrollHashKey, Map<String, String> positionMap) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(positionEnrollHashKey, positionMap);
    }

    /**
     * 报名信息放到缓存中
     * 自身考试数据缓存+用户报名地区/学校缓存
     *
     * @param matchUserMeta
     * @return
     */
    private int putEnrollInfo2Cache(MatchUserMeta matchUserMeta) {
        if (null == matchUserMeta) {
            return 0;
        }
        //用户报名地区/学校缓存维护/分数等信息维护
        saveEnroll2Cache(matchUserMeta);
        //用户考试数据缓存做判断，如果有值，返回0，没有值返回1
        MatchUserMeta matchUserEnrollInfo = findMatchUserEnrollInfo(matchUserMeta.getUserId(), matchUserMeta.getMatchId());
        if (null != matchUserEnrollInfo) {
            return 0;
        }
        return 1;
    }

    /**
     * 单个用户报名数据缓存
     *
     * @param matchUserMeta
     */
    private void addEnrollInfo2Cache(MatchUserMeta matchUserMeta) {
        String userEnrollKey = MatchInfoRedisKeys.getUserEnrollHashKey(matchUserMeta.getMatchId(), matchUserMeta.getUserId());
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> enrollMap = MatchUserMetaUtil.convertEnrollObject2Map(matchUserMeta);
        enrollMap.put(MatchUserMetaUtil.MetaAttrEnum.OTHER.getKey(), MatchUserMetaUtil.IS_NOT_OTHER);        //缓存空值标识去除
        hashOperations.putAll(userEnrollKey, enrollMap);
        redisTemplate.expire(userEnrollKey, 15, TimeUnit.DAYS);
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 事件处理实现
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(MatchUserChangeEvent event) {
        MatchUserMeta matchUserMeta = event.getMatchUserMeta();
        MatchUserChangeEvent.OperationEnum operationEnum = event.getOperationEnum();
        if (null == matchUserMeta || null == matchUserMeta.getMatchId() || null == matchUserMeta.getUserId()) {
            return;
        }
        switch (operationEnum) {
            case INSERT:
                insert(matchUserMeta);
            case UPDATE:
                Example updateExample = new Example(MatchUserMeta.class);
                updateExample.and().andEqualTo("matchId", matchUserMeta.getMatchId()).andEqualTo("userId", matchUserMeta.getUserId());
                updateByExampleSelective(matchUserMeta, updateExample);
                break;
            default:
                log.error("event handler error, event = {}", event.getOperationEnum().getKey());
        }
    }
}
