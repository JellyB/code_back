package com.huatu.ztk.paper.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeBasedTable;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.paper.util.SensorsUtils;
import com.huatu.ztk.paper.util.VersionUtil;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by linkang on 17-7-14.
 */
@Service
public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    /**
     * 消息队列
     */
    public static final String SUBMIT_MATCH_ANSWERCARD_MQ = "submit_match_answercard";

    @Autowired
    MatchChangeConfig matchChangeConfig;

    /**
     * 降级模考大赛首页缓存(key为redisKey设定的key，value为首页数据)
     */
    Cache<String, List<Match>> MATCH_HEADER_MOCK_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(3, TimeUnit.MINUTES)
                    .build();
    /**
     * 模考大赛分数缓存，访问更新缓存时间
     */
    Cache<Integer, Double> MATCH_SCORE_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(3, TimeUnit.DAYS)
                    .build();
    //    /**
//     * 模考大赛常量
//     */
//    public final static boolean DEFAULT_MATCH_OLD_FLAG = true;
//    public final static String MATCH_ANDROID_CV_DEADLINE = "7.1.8";
//    public final static String MATCH_IPHONE_CV_DEADLINE = "7.1";
//
//    public final static String MATCH_OLD_SUBJECT = "";
//    public final static String MATCH_NEW_SUBJECT = "";
    @Autowired
    private MatchDao matchDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate coreRedisTemplate;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public MatchConfig matchConfig;
    @Autowired
    public PaperService paperService;

    @Autowired
    private PaperAnswerCardUtilComponent paperAnswerCardUtilComponent;

    @Autowired
    private MatchServiceComponent matchServiceComponent;

    @Autowired
    private BigBagUsedSubjectConfig bigBagUsedSubjectConfig;


    /**
     * 大赛入口信息展示详情
     *
     * @param userId
     * @param subject
     * @return
     * @throws BizException
     */
    public Match getMatch(long userId, int subject) throws BizException {
        Match match = matchDao.findCurrentForPc(subject);

        if (match == null) {
            throw new BizException(MatchErrors.NO_MATCH);
        }
        packageMatchInfo(match, userId, false);
        return match;
    }

    private void packageMatchInfoWithEssay(Match match, long userId) {
        long currentTime = System.currentTimeMillis();
        //行测和申论的分割时间点
        long splitTime = match.getEssayStartTime() - TimeUnit.MINUTES.toMillis(matchConfig.getEssayLeadTime());
        if (currentTime < splitTime) {
            //行测阶段的判断
            packageMatchInfo(match, userId, true);
            //阶段设定
            match.setStage(1);
        } else {
            //申论阶段的判断
            packageEssayMatchInfo(match, userId);
            //阶段设定
            match.setStage(2);
        }
    }

    /**
     * 用户申论状态的判断
     *
     * @param match
     * @param userId
     */
    private void packageEssayMatchInfo(Match match, long userId) {
        int paperId = match.getPaperId();
        long startTime = match.getEssayStartTime();
        long endTime = match.getEssayEndTime();
        long currentTime = System.currentTimeMillis();
        long essayPaperId = match.getEssayPaperId();
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);
        //已报名
        if (userMeta != null) {
            String setKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, userMeta.getPositionId());
            SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
            Long positionCount = opsForSet.size(setKey);
            userMeta.setPositionCount(positionCount.intValue());

            //答题卡id
            String key = RedisKeyConstant.getUserAnswerStatusKey(essayPaperId);
            String essayStatus = (String) redisTemplate.opsForHash().get(key, userId + "");
            if (essayStatus != null) {
                int status = Integer.parseInt(essayStatus);
                //判断是否交卷且批改
                boolean isSubmit = (status >= 2);
                boolean currentFinish = isEssayMatchFinish(match, status);
                if (isSubmit) {
                    match.setStatus(currentFinish ? MatchStatus.REPORT_AVAILABLE : MatchStatus.REPORT_UNAVILABLE);
                } else if (currentTime < endTime) {
                    match.setStatus(MatchStatus.NOT_SUBMIT);
                } else {
                    match.setStatus(MatchStatus.REPORT_UNAVILABLE);
                }
            } else {
                if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(5)) {
                    //距开始小于一个小时,大于5分钟
                    match.setStatus(MatchStatus.START_UNAVILABLE);
                } else if (currentTime - startTime < TimeUnit.MINUTES.toMillis(30) &&
                        currentTime < endTime) {
                    //距开始小于5分钟,且距离开始后小于30分钟
                    match.setStatus(MatchStatus.START_AVILABLE);
                } else {
                    //已经开始30分钟
                    match.setStatus(MatchStatus.MATCH_UNAVILABLE);
                }
            }
        } else {
            match.setStatus(MatchStatus.PASS_UP_ENROLL);
        }

        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        /* 返回报名总数*/
        String enrollCountStr = opsForValue.get(MatchRedisKeys.getTotalEnrollCountKey(paperId));

        int enrollCount = 0;
        if (enrollCountStr != null) {
            enrollCount = Integer.valueOf(enrollCountStr);
        }

        match.setEnrollCount(enrollCount);
        match.setUserMeta(userMeta);
    }

    /**
     * 判断申论考试是否结束且试卷已经全部处理
     *
     * @param match
     * @param status
     * @return
     */
    private boolean isEssayMatchFinish(Match match, int status) {
        //待处理的试卷set
        String setKey = RedisKeyConstant.getPublicUserSetPrefix(match.getEssayPaperId());
        boolean isTimeEnd = System.currentTimeMillis() > match.getEssayEndTime() + TimeUnit.MINUTES.toMillis(matchConfig.getEssayDelayReportTime());
        boolean isFinished = (status == 3);
        return isTimeEnd && redisTemplate.opsForSet().size(setKey).intValue() == 0 && isFinished;
    }

    /**
     * 用户在行测模式模考大赛的状态判断
     * 或者用户在行测+申论联合模式模考大赛，行测考试阶段的用户状态判断
     *
     * @param match
     * @param userId
     * @param isFlag 是否是新版本（新版本，区分无法考试状态和停止报名状态）
     */
    private void packageMatchInfo(Match match, long userId, boolean isFlag) {
        StopWatch stopWatch = new StopWatch("packageMatchInfo");
        int paperId = match.getPaperId();
        long startTime = match.getStartTime();
        long endTime = match.getEndTime();
        long currentTime = System.currentTimeMillis();
        stopWatch.start("findMatchUserMeta");
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);
        stopWatch.stop();
        //已报名
        if (userMeta != null) {
//            String setKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, userMeta.getPositionId());
//            SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
//
//            Long positionCount = opsForSet.size(setKey);
//            userMeta.setPositionCount(positionCount.intValue());
            userMeta.setPositionCount(1);       //地区报名人数不展示，所以默认为1，不做查询（高并发时，查询性能差）
            //答题卡id
            long practiceId = userMeta.getPracticeId();
            if (practiceId > 0) {
                stopWatch.start("isPracticeSubmit");
                //判断是否交卷
                boolean isSubmit = isPracticeSubmit(paperId, practiceId);
                stopWatch.stop();
                stopWatch.start("isCurrentMatchFinish");
                boolean currentFinish = isCurrentMatchFinish(match);
                stopWatch.stop();
                if (isSubmit) {
                    match.setStatus(currentFinish ? MatchStatus.REPORT_AVAILABLE : MatchStatus.REPORT_UNAVILABLE);
                } else if (currentTime < endTime) {
                    match.setStatus(MatchStatus.NOT_SUBMIT);
                } else {
                    match.setStatus(MatchStatus.REPORT_UNAVILABLE);
                }

            } else {
                if (startTime - currentTime >= TimeUnit.HOURS.toMillis(1)) {
                    //距开始大于一个小时
                    match.setStatus(MatchStatus.ENROLL);
                } else if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(5)) {
                    //距开始小于一个小时,大于5分钟
                    match.setStatus(MatchStatus.START_UNAVILABLE);
                } else if (currentTime - startTime < TimeUnit.MINUTES.toMillis(30) &&
                        currentTime < endTime) {
                    //距开始小于5分钟,且距离开始后小于30分钟
                    match.setStatus(MatchStatus.START_AVILABLE);
                } else {
                    //已经开始30分钟
                    match.setStatus(MatchStatus.MATCH_UNAVILABLE);
                }
            }

        } else {
            match.setStatus(MatchStatus.UN_ENROLL);
            /**
             * @update 2018/08/20 huangqp
             * 前提:未报名
             * 原逻辑：已经开始30分钟或者考试已结束，如果是联合考试，置状态为9，否则为5
             * 现逻辑：已经开始30分钟或者考试已结束，状态都置为9
             */
            //已经开始30分钟或者考试已结束
            if (currentTime - startTime >= TimeUnit.MINUTES.toMillis(30)
                    || currentTime > endTime) {
                //状态置为“未报名且错过报名”
                match.setStatus(MatchStatus.PASS_UP_ENROLL);
            }
        }
        stopWatch.start("getTotalEnrollCountKey");
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        /* 返回报名总数*/
        String enrollCountStr = opsForValue.get(MatchRedisKeys.getTotalEnrollCountKey(paperId));
        stopWatch.stop();
        int enrollCount = 0;
        if (enrollCountStr != null) {
            enrollCount = Integer.valueOf(enrollCountStr);
        }
        match.setEnrollCount(enrollCount);
        match.setUserMeta(userMeta);
        if (stopWatch.getTotalTimeSeconds() > 1) {
            logger.info(stopWatch.prettyPrint());
        }
    }

    /**
     * 大赛报名
     *
     * @param paperId
     * @param userId
     * @param positionId
     * @Description 报名数据处理：
     * 1、如果
     */
    public Match enrollV2(int paperId, long userId, int positionId, int subject, int terminal) throws BizException {
        logger.info("match enroll2 paperId={},userId={},positionId={}", paperId, userId, positionId);

        Match match = matchDao.findById(paperId);
        if (match == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        enrollHelp(paperId, userId, positionId, match);
        Long essayPaperId = match.getEssayPaperId();
        /**
         * 申论报名的逻辑从上一个方法实现中剥离出来，单独实现，整合在一起
         */
        if (essayPaperId != null && essayPaperId > 0) {
            //更新申论报考地区信息
            updateEssayEnrollRedis(essayPaperId, positionId, userId);
        }
//        /* 根据用户id申论模考试卷id获取该用户报名的地区id【hash】     （field）essayPaperId     userId【key】     地区(职位)id  【value】 */
//        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        if (match.getEssayPaperId() > 0) {
//            String essayPaperKey = RedisKeyConstant.getMockUserAreaPrefix(match.getEssayPaperId());
//            hashOperations.put(essayPaperKey, userId + "", positionId + "");
//        }
        /**
         * update by lijun
         * 2018-11-24
         * 模考大赛在报名的时候直接创建答题卡 - 避免与原始的逻辑重复,此处使用缓存
         */
//        try {
//            matchServiceComponent.createCachedMatchCardAndPutRedis(paperId, subject, userId, terminal);
//        } catch (Exception e) {
//            logger.info(" 报名创建答题卡失败，》》》》{}", e);
//        }
        return match;

    }

    /**
     * 模考大赛行测报名逻辑
     *
     * @param paperId
     * @param userId
     * @param positionId
     * @param match
     * @throws BizException
     */
    private void enrollHelp(int paperId, long userId, int positionId, Match match) throws BizException {
        //模考大赛考试开始30分钟后，不能报名
        if (System.currentTimeMillis() - match.getStartTime() > TimeUnit.MINUTES.toMillis(30)) {
            throw new BizException(MatchErrors.MISSING_MATCH);
        }
        /*获取用户报名信息*/
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);

        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        /*  该模考报名总数  */
        /**
         * 报名修改不累加报名次数（20180711刘老师提议）
         */
//        String countKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
//        opsForValue.incrementJoinCount(countKey, 1);
//        /* 包含申论，申论报名人数+1 */
//        if (flag) {
//            String essayCountKey = RedisKeyConstant.getTotalEnrollCountKey(essayPaperId);
//            opsForValue.incrementJoinCount(essayCountKey, 1);
//        }
        if (userMeta == null) {
            //总数量+1
            String countKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
            opsForValue.increment(countKey, 1);
            userMeta = MatchUserMeta.builder()
                    .id(getMatchUserMetaId(userId, paperId))
                    .userId(userId)
                    .positionId(positionId)
                    .positionName(PositionConstants.getFullPositionName(positionId))
                    .practiceId(-1)
                    .paperId(paperId)
                    .build();
            //地区分组，用户集合添加
            String positionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, positionId);
            opsForSet.add(positionEnrollSetKey, userId + "");
            /**
             * 申论报名数据新增修改都可以一个方法实现，所以放到外面统一处理
             */
//            if (flag) {
//                String positionEssayEnrollSetKey = RedisKeyConstant.getPositionEnrollSetKey(essayPaperId, positionId);
//                opsForSet.add(positionEssayEnrollSetKey, userId + "");
//            }
        } else {
            updateUserMetaPositionInfo(opsForSet, userMeta, positionId, paperId, userId);
//            int position = userMeta.getPositionId();
//            String positionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, position);
//            /*  移除之前该用户的行测旧职位报名信息 */
//            opsForSet.remove(positionEnrollSetKey, userId + "");
//            if (flag) {
//                String positionEssayEnrollSetKey = RedisKeyConstant.getPositionEnrollSetKey(essayPaperId, position);
//                /*  移除之前该用户的申论旧职位报名信息 */
//                opsForSet.remove(positionEssayEnrollSetKey, userId + "");
//            }
//            userMeta.setPositionId(positionId);
//            userMeta.setPositionName(PositionConstants.getFullPositionName(positionId));
        }
        matchDao.saveUserMeta(userMeta);
    }

    /**
     * 更新用户报考的地区
     *
     * @param opsForSet
     * @param userMeta
     * @param newPositionId
     * @param paperId
     * @param userId
     * @update haungqp 20180823 只处理行测模考大赛的报名数据变动逻辑（之前已报过名的用户），将申论报名处理独立出去
     */
    private void updateUserMetaPositionInfo(SetOperations<String, String> opsForSet, MatchUserMeta userMeta, int newPositionId, int paperId, long userId) {
        //如果和之前报考的一样，则不需要更新
        if (newPositionId == userMeta.getPositionId()) {
            return;
        }
        int oldPositionId = userMeta.getPositionId();
        //更新行测报名地区信息
        String oldPositionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, oldPositionId);
        userMeta.setPositionId(newPositionId);
        userMeta.setPositionName(PositionConstants.getFullPositionName(newPositionId));

        String positionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, newPositionId);
        /*  移除之前该用户的行测旧职位报名信息 */
        opsForSet.remove(oldPositionEnrollSetKey, userId + "");
        opsForSet.add(positionEnrollSetKey, userId + "");
        /**
         * 申论报名数据更新的逻辑不管是新增还是修改都可以通过一个方法实现所有移到外部统一处理
         */
//        //更新申论报考地区信息
//        if(flag){
//            updateEssayEnrollRedis(essayPaperId,opsForSet,newPositionId,userId);
//        }


    }

    /**
     * 添加或者更新申论报名数据的逻辑都是这个
     *
     * @param essayPaperId
     * @param newPositionId
     * @param userId
     */
    private void updateEssayEnrollRedis(long essayPaperId, int newPositionId, long userId) {
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        HashOperations hashOperations = redisTemplate.opsForHash();
        String essayPaperKey = RedisKeyConstant.getMockUserAreaPrefix(essayPaperId);
        Object object = hashOperations.get(essayPaperKey, userId + "");
        logger.info("cone:essayPaperKey={},userId={}", essayPaperKey, userId);
        if (object != null) {
            logger.info("cone:object={},userId={}", object, userId);
            String oldPositionEssayEnrollSetKey = RedisKeyConstant.getPositionEnrollSetKey(essayPaperId, Integer.parseInt(object.toString().replace("\"", "")));
            opsForSet.remove(oldPositionEssayEnrollSetKey, userId + "");
        } else {
            String countEnrollKey = RedisKeyConstant.getTotalEnrollCountKey(essayPaperId);
            logger.info("cone:countEnrollKey={},userId={}", countEnrollKey, userId);
            redisTemplate.opsForValue().increment(countEnrollKey, 1);
        }
        String positionEssayEnrollSetKey = RedisKeyConstant.getPositionEnrollSetKey(essayPaperId, newPositionId);
        opsForSet.add(positionEssayEnrollSetKey, userId + "");
        hashOperations.put(essayPaperKey, userId + "", newPositionId + "");
    }


    /**
     * 大赛报名
     *
     * @param paperId
     * @param userId
     * @param positionId 最初设计用作 职位id，目前做地区id使用  详细见 position.txt
     */
    public void enroll(int paperId, long userId, int positionId) throws BizException {
        logger.info("match enroll paperId={},userId={},positionId={}", paperId, userId, positionId);

        Match match = matchDao.findById(paperId);
        if (match == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        enrollHelp(paperId, userId, positionId, match);

    }

    /**
     * 用户的大赛信息 id
     *
     * @param userId
     * @param paperId
     * @return
     */
    private String getMatchUserMetaId(long userId, int paperId) {
        return new StringBuilder().append(userId)
                .append("_").append(paperId).toString();
    }


    public MatchUserMeta findMatchUserMeta(long userId, int paperId) {
        return matchDao.findMatchUserMeta(getMatchUserMetaId(userId, paperId));
    }

    /**
     * 更新练习id
     *
     * @param standardCard
     */
    public int updateMatchUserMeta(StandardCard standardCard) {
        int paperId = standardCard.getPaper().getId();
        MatchUserMeta matchUserMeta = findMatchUserMeta(standardCard.getUserId(), paperId);
        if (matchUserMeta != null) {
            matchUserMeta.setPracticeId(standardCard.getId());
            //更新练习id
            return matchDao.updatePracticeId(matchUserMeta);
        }
        return 0;
    }


    /**
     * 模考大赛统计信息
     * 统计模考大赛才有的数据（曲线图，地区成绩）
     *
     * @param standardCard
     * @return
     */
    public MatchCardUserMeta findMatchCardUserMeta(final StandardCard standardCard) {
        Paper paper = standardCard.getPaper();
        final int currentPaperId = paper.getId();
        //通过试卷id获取模考大赛信息
        Match currentMatch = matchDao.findById(paper.getId());
        //查询所有已经进行的模考大赛
        List<Match> matchList = matchDao.findAll(currentMatch.getStartTime(), currentMatch.getTag(), standardCard.getSubject());

        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        long userId = standardCard.getUserId();

        MatchCardUserMeta userMeta = new MatchCardUserMeta();
        //获取用户历次模考大赛的报名信息
        Map<Integer, MatchUserMeta> paperIdMetaMap = getPaperIdMetaMap(matchList, userId);
        //设置用户历次模考大赛成绩曲线图
        userMeta.setScoreLine(getLine(matchList, paperIdMetaMap));

        MatchUserMeta currentMeta = findMatchUserMeta(userId, currentPaperId);
        //用户此次模考大赛的地区排名成绩，地区最高分和击败人数
        int positionId = currentMeta.getPositionId();

        String positionScoreSum = MatchRedisKeys.getPositionScoreSum(currentPaperId, positionId);
        String positionlSum = valueOperations.get(positionScoreSum);

        String positionPracticeIdSore = MatchRedisKeys.getPositionPracticeIdSore(currentPaperId, positionId);
        int count = zSetOperations.size(positionPracticeIdSore).intValue();

        Double positionAverage = 0d;
        if (positionlSum != null && count != 0) {
            positionAverage = Double.parseDouble(positionlSum) / count;
        }


        Long rank = zSetOperations.reverseRank(positionPracticeIdSore, standardCard.getId() + "");
        if (rank == null) {
            rank = Long.valueOf(count);
        } else {
            rank = rank + 1;
        }


        Double max = 0d;
        if (count != 0) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.reverseRangeWithScores(positionPracticeIdSore, 0, 0);

            if (CollectionUtils.isNotEmpty(withScores)) {
                max = new ArrayList<>(withScores).get(0).getScore();
            }
        }
        BigDecimal bigDecimal = new BigDecimal(positionAverage);
        double pAverage = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        BigDecimal maxDecimal = new BigDecimal(max);
        double pMax = maxDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        userMeta.setPositionAverage(pAverage);
        userMeta.setPositionMax(pMax);
        userMeta.setPositionName(currentMeta.getPositionName());
        userMeta.setPositionRank(rank.intValue());
        userMeta.setPositionCount(count);
        userMeta.setPositionBeatRate(count != 0 ? ((count - rank.intValue()) * 100 / count) : 0);
        userMeta.setPositionId(positionId);
        Long schoolId = currentMeta.getSchoolId();
        if (schoolId != null && schoolId > 0) {
            String schoolPracticeIdSore = MatchRedisKeys.getSchoolPracticeIdSore(currentPaperId, schoolId);
            int schoolCount = zSetOperations.size(schoolPracticeIdSore).intValue();

            Long schoolRank = zSetOperations.reverseRank(schoolPracticeIdSore, standardCard.getId() + "");
            if (schoolRank == null) {
                schoolRank = Long.valueOf(schoolCount);
            } else {
                schoolRank = schoolRank + 1;
            }
            userMeta.setPositionId(positionId);
            userMeta.setSchoolCount(schoolCount);
            userMeta.setSchoolRank(schoolRank.intValue());
            userMeta.setSchoolName(currentMeta.getSchoolName());
        }

        return userMeta;
    }

    /**
     * 设置答题卡的模考大赛的统计
     *
     * @param standardCard
     */
    public void setMatchCardMeta(StandardCard standardCard) {
        long userId = standardCard.getUserId();
        int paperId = standardCard.getPaper().getId();
        int qcount = standardCard.getPaper().getQcount();
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        MatchUserMeta matchUserMeta = findMatchUserMeta(userId, paperId);
        if (matchUserMeta != null) {

            //添加到排名中
            //未做题量=试题试卷题目数量,不添加到排名
            if (qcount != standardCard.getUcount()) {
                int positionId = matchUserMeta.getPositionId();
                Long schoolId = matchUserMeta.getSchoolId();
                //职位排名有序集合
                String positionPracticeIdSore = MatchRedisKeys.getPositionPracticeIdSore(paperId, positionId);


                /**
                 * 查下是否之前有成绩，如果有则覆盖掉，并在累加值计算的时候减掉之前的成绩
                 */
                Double score = zSetOperations.score(positionPracticeIdSore, standardCard.getId() + "");
                if (null == score) {
                    score = 0D;
                }
                zSetOperations.add(positionPracticeIdSore, standardCard.getId() + "", standardCard.getScore());
                if (schoolId != null && schoolId > 0) {
                    //学院排名有序集合
                    String schoolPracticeIdSore = MatchRedisKeys.getSchoolPracticeIdSore(paperId, schoolId);
                    zSetOperations.add(schoolPracticeIdSore, standardCard.getId() + "", standardCard.getScore());

                }
                //模考大赛职位总得分
                String positionScoreSum = MatchRedisKeys.getPositionScoreSum(paperId, positionId);
                valueOperations.increment(positionScoreSum, standardCard.getScore() - score);

            }

            standardCard.setMatchMeta(findMatchCardUserMeta(standardCard));
        }
    }

    /**
     * 获得分数折线
     * TODO 不用每次都实时计算，可以直接将结果缓存
     *
     * @param matchList      模考列表
     * @param paperIdMetaMap paperId--》模考信息map
     * @return
     */
    private Line getLine(List<Match> matchList, Map<Integer, MatchUserMeta> paperIdMetaMap) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        final TreeBasedTable<Long, String, Number> basedTable = TreeBasedTable.create();

        for (Match match : matchList) {
            int paperId = match.getPaperId();

            MatchUserMeta meta = paperIdMetaMap.get(paperId);
            //模考未结束，用户未报名，用户未答题
            if (match.getEndTime() > System.currentTimeMillis() ||
                    meta == null || meta.getPracticeId() < 0) {
                continue;
            }

            String pPracticeIdSoreKey = MatchRedisKeys.getPositionPracticeIdSore(paperId, meta.getPositionId());
            Double userScore = zSetOperations.score(pPracticeIdSoreKey, meta.getPracticeId() + "");

            //没有在排名中
            if (userScore == null) {
                userScore = 0D;
            }

            String paperScoreSumKey = PaperRedisKeys.getPaperScoreSum(paperId);
            String paperPracticeIdSoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);

            Long totalCardCount = zSetOperations.size(paperPracticeIdSoreKey);
            String paperScoreSum = valueOperations.get(paperScoreSumKey);

            Double average = 0d;
            if (paperScoreSum != null && totalCardCount != 0) {
                average = Double.parseDouble(paperScoreSum) / totalCardCount;
            }
            //用户分数
            BigDecimal bigDecimal = new BigDecimal(userScore);
            Double dUserScore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

            //平均分
            BigDecimal maxDecimal = new BigDecimal(average);
            Double dAverage = maxDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            Double matchScore = getMatchScore(paperId);
            //试卷总分存在且学员分数小于总分的合理情况才会被录入曲线图，否则不录入
            if (null != matchScore && matchScore > dUserScore) {
//                basedTable.put(match.getStartTime(), "模考得分", dUserScore);
//                basedTable.put(match.getStartTime(), "全站平均得分", dAverage);
                basedTable.put(match.getStartTime(), "模考准确率", dUserScore * 100 / matchScore.intValue());
                basedTable.put(match.getStartTime(), "全站准确率", dAverage * 100 / matchScore.intValue());
            }
        }

        logger.info("basedTable={}", basedTable);
        Line line = table2LineSeries(basedTable);
        logger.info("line={}", line);
        return line;
    }

    private static final Line table2LineSeries(TreeBasedTable<Long, String, ? extends Number> table) {
        final Set<String> columnKeySet = table.columnKeySet();
        final Set<Long> rowKeySet = table.rowKeySet();
        List<LineSeries> seriesList = new ArrayList<>(rowKeySet.size());
        for (Long dateStamp : rowKeySet) {
            List data = new ArrayList(columnKeySet.size());
            List<String> strData = new ArrayList<>(columnKeySet.size());
            for (String column : columnKeySet) {
                Number number = table.get(dateStamp, column);
                if (number == null) {//为空则进行初始化
                    number = Double.valueOf(0);
                }
                data.add(number);
                strData.add(String.valueOf(number));
            }

            final LineSeries lineSeries = LineSeries.builder()
                    .name(DateFormatUtils.format(dateStamp, "M-d"))
                    .data(data)
                    .strData(strData)
                    .build();
            seriesList.add(lineSeries);
        }

        final Line line = Line.builder()
                .categories(Lists.newArrayList(columnKeySet))
                .series(seriesList)
                .build();

        return line;
    }

    /**
     * 获取模考大赛和对应的用户报名信息
     *
     * @param matchList
     * @param userId
     * @return
     */
    private Map<Integer, MatchUserMeta> getPaperIdMetaMap(List<Match> matchList, long userId) {
        //用户模考大赛信息id列表
        List<String> metaIds = matchList.stream()
                .map(m -> getMatchUserMetaId(userId, m.getPaperId()))
                .collect(Collectors.toList());

        //key: paperid, value: meta2
        Map<Integer, MatchUserMeta> paperIdMetaMap = matchDao.findAllMatchUserMeta(metaIds)
                .stream().collect(Collectors.toMap(i -> i.getPaperId(), i -> i));

        return paperIdMetaMap;
    }


    /**
     * 模考历史
     * 6.1.1版本 支持多个模考大赛
     *
     * @param userId
     * @param tag
     * @param subject
     * @param terminal
     * @param cv
     * @return
     */
    public Object getHistoryV62(long userId, int tag, int subject, int terminal, String cv) {
        List<Match> matchList = matchDao.findAll(-1, tag, subject);
        if (subject == SubjectType.GWY_XINGCE && tag == 2) {     //行测省考，过滤掉2019之前的所有
            matchList.removeIf(i -> i.getEndTime() <= 1523674800000L);
        }
        //获取到当天所有的需要删除的数据
        List<Match> currentMatches = matchDao.findUsefulMatch(subject);
        if (CollectionUtils.isNotEmpty(currentMatches)) {
            //定义删除指令
            List<Integer> removeIdList = currentMatches.stream()
                    .filter(match1 -> !((match1 != null) ? isCurrentMatchFinish(match1) : true))
                    .map(Match::getPaperId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(removeIdList)) {
                matchList.removeIf((matchData) -> removeIdList.contains(matchData.getPaperId()));
            }
        }
        Map<Integer, MatchUserMeta> paperIdMetaMap = getPaperIdMetaMap(matchList, userId);
        Line line = getLine(matchList, paperIdMetaMap);

        List<MatchHistory> historyList = new ArrayList<>();


        for (Match match : matchList) {

            int paperId = match.getPaperId();

            MatchUserMeta meta = paperIdMetaMap.get(paperId);

            //模考未结束，用户未报名，用户未答题
            if (match.getEndTime() > System.currentTimeMillis() ||
                    meta == null || meta.getPracticeId() < 0) {
                continue;
            }

            String key = PaperRedisKeys.getPaperPracticeIdSore(paperId);
            ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();

            Long count = opsForZSet.size(key);
            MatchHistory history = MatchHistory.builder()
                    .name(match.getName())
                    .practiceId(meta.getPracticeId())
                    .paperId(match.getPaperId())
                    .startTime(match.getStartTime())
                    .total(count.intValue())
                    .build();
            long essayPaperId = match.getEssayPaperId();
            if (essayPaperId > 0) {
                history.setEssayPaperId(essayPaperId);
                //模考大赛申论阶段是否结束
                boolean flag = System.currentTimeMillis() - match.getEssayEndTime() - TimeUnit.MINUTES.toMillis(matchConfig.getEssayDelayReportTime()) > 0;
                if (hasEssayScore(essayPaperId, userId) && flag) {
                    //考试结果都存在
                    history.setFlag(3);
                } else {
                    //只有行测考试报告
                    history.setFlag(1);
                }
            } else {
                history.setFlag(1);
            }
            historyList.add(history);
        }

        //倒序
        Collections.reverse(historyList);
        if(!AnswerCardUtil.judgeUserCv(terminal,cv)){
            AnswerCardUtil.handlerLine(line,AnswerCardUtil.transInt);
        }
        HashMap map = new HashMap();
        map.put("line", line);
        map.put("list", historyList);
        return map;
    }

    /**
     * 模考历史
     *
     * @param userId
     * @param tag
     * @param subject
     * @param terminal
     * @param cv
     * @return
     */
    public Object getHistory(long userId, int tag, int subject, int terminal, String cv) {
        List<Match> matchList = matchDao.findAll(System.currentTimeMillis(), tag, subject);
        if (subject == SubjectType.GWY_XINGCE && tag == 2) {     //行测省考，过滤掉2019之前的所有
            matchList.removeIf(i -> i.getEndTime() <= 1523674800000L);
        }
        /**
         * 以最近的一次考试为起点，删除掉相邻两次考试时间间隔超过30天，之后的所有考试
         */
        if (CollectionUtils.isNotEmpty(matchList) && matchList.size() > 2) {
            long startTime = -1L;
            //倒序是为了删除间隔30天后的所有试卷，之后报告还是要正序返回
            matchList.sort(Comparator.comparing(i -> -i.getStartTime()));
            for (int i = 0; i < matchList.size() - 1; i++) {
                //相邻两个考试间隔超过一个月，那么考试时间小于startTime的考试都隐藏
                if (matchList.get(i).getStartTime() - matchList.get(i + 1).getStartTime() > TimeUnit.DAYS.toMillis(30)) {
                    startTime = matchList.get(i).getStartTime();
                    break;
                }
            }
            if (startTime > 0) {
                long splitTime = startTime;
                matchList.removeIf(i -> i.getStartTime() < splitTime);
            }
        }
        if (CollectionUtils.isNotEmpty(matchList)) {
            //剔除所有没结束的模考大赛
            matchList.removeIf(match ->
                    (match != null) ? !isCurrentMatchFinish(match) : false
            );
        }
        matchList.sort(Comparator.comparing(Match::getStartTime));
        Map<Integer, MatchUserMeta> paperIdMetaMap = getPaperIdMetaMap(matchList, userId);
        Line line = getLine(matchList, paperIdMetaMap);

        List<MatchHistory> historyList = new ArrayList<>();


        for (Match match : matchList) {

            int paperId = match.getPaperId();

            MatchUserMeta meta = paperIdMetaMap.get(paperId);

            //模考未结束，用户未报名，用户未答题
            if (match.getEndTime() > System.currentTimeMillis() ||
                    meta == null || meta.getPracticeId() < 0) {
                continue;
            }

            String key = PaperRedisKeys.getPaperPracticeIdSore(paperId);
            ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();

            Long count = opsForZSet.size(key);
            MatchHistory history = MatchHistory.builder()
                    .name(match.getName())
                    .practiceId(meta.getPracticeId())
                    .paperId(match.getPaperId())
                    .startTime(match.getStartTime())
                    .total(count.intValue())
                    .build();
            long essayPaperId = match.getEssayPaperId();
            if (essayPaperId > 0) {
                history.setEssayPaperId(essayPaperId);
                //模考大赛申论阶段是否结束
                boolean flag = System.currentTimeMillis() - match.getEssayEndTime() - TimeUnit.MINUTES.toMillis(matchConfig.getEssayDelayReportTime()) > 0;
                if (hasEssayScore(essayPaperId, userId) && flag) {
                    //考试结果都存在
                    history.setFlag(3);
                } else {
                    //只有行测考试报告
                    history.setFlag(1);
                }
            } else {
                history.setFlag(1);
            }
            historyList.add(history);
        }

        //倒序
        Collections.reverse(historyList);

        HashMap map = new HashMap();
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerLine(line,AnswerCardUtil.transInt);
        }
        map.put("line", line);
        map.put("list", historyList);
        return map;
    }


    /**
     * 将答题卡id放入 模考大赛答题卡 set
     */
    public void addMatchPracticeSet(StandardCard answerCard) {
        int paperId = answerCard.getPaper().getId();

        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        String setKey = MatchRedisKeys.getMatchPracticeIdSetKey(paperId);

        opsForSet.add(setKey, answerCard.getId() + "");

        logger.info("add MatchPracticeSet,key={},practiceId={}", setKey, answerCard.getId());
    }

    /**
     * 从模考大赛答题卡 set删除
     */
    public void removeFromMatchPracticeSet(StandardCard answerCard) {
        int paperId = answerCard.getPaper().getId();

        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        String setKey = MatchRedisKeys.getMatchPracticeIdSetKey(paperId);

        opsForSet.remove(setKey, answerCard.getId() + "");

        logger.info("remove from MatchPracticeSet,key={},practiceId={}", setKey, answerCard.getId());
    }


    /**
     * 创建模考大赛答题卡
     *
     * @param paperId
     * @param subject
     * @param userId
     * @param terminal
     * @return
     * @throws BizException
     * @throws WaitException
     */
    public StandardCard createPractice(int paperId, int subject, long userId, int terminal) throws BizException, WaitException {

        Paper paper = paperDao.findById(paperId);
        if (paper == null || !(paper instanceof EstimatePaper)) {
            logger.info("userId:{},error:{}", userId, CommonErrors.RESOURCE_NOT_FOUND.getMessage());
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        //校正用户当前科目
        int paperCategory = paper.getCatgory();
        if (subject != paperCategory) {
            subject = paperCategory;
        }

        EstimatePaper estimatePaper = (EstimatePaper) paper;

        //判断当前科目是否是招警
        int beforeTime = 5;
        // 招警机考提前十分钟可以创建答题卡
        if (subject == 100100173) {
            beforeTime = 10;
        }

        //距离开始时间超过5分钟
        if (estimatePaper.getStartTime() - System.currentTimeMillis() > TimeUnit.MINUTES.toMillis(beforeTime)) {
            /**
             * updateBy lijun 2018-02-26
             * 此部分代码只作为 内部线上数据测试使用,与原始业务逻辑相违背.
             * 白名单 状态值 矫正
             */
            SetOperations opsForSet = redisTemplate.opsForSet();
            Boolean member = opsForSet.isMember(MatchRedisKeys.getMatchWhitUserReportKey(), String.valueOf(userId));
            if (!member) {
                logger.info("userId:{},error:{}", userId, MatchErrors.NOT_START.getMessage());
                throw new BizException(MatchErrors.NOT_START);
            }
        }

        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);
        if (userMeta == null) { //未报名
            logger.info("userId:{},error:{}", userId, MatchErrors.NOT_ENROLL.getMessage());
            throw new BizException(MatchErrors.NOT_ENROLL);
        }

        long practiceId = userMeta.getPracticeId();
        if (practiceId > 0) { //已经创建了答题卡，返回原来的答题卡
            StandardCard standardCard = (StandardCard) answerCardDao.findById(practiceId);
            standardCard.setIdStr(standardCard.getId() + "");
            standardCard.setCurrentTime(System.currentTimeMillis());
            return standardCard;
        }

        //模考大赛考试开始30分钟后，无法创建答题卡
        if (System.currentTimeMillis() - estimatePaper.getStartTime() > TimeUnit.MINUTES.toMillis(30)) {
            logger.info("userId:{},error:{}", userId, MatchErrors.MISSING_MATCH.getMessage());
            throw new BizException(MatchErrors.MISSING_MATCH);
        }
        /**
         * update by lijun
         * 2018-11-24
         * 创建答题卡操作提前
         */
//        StandardCard standardCard = matchServiceComponent.getCachedMatchCardFromRedisAndDelete(paperId, subject, userId);
//        if (null == standardCard) {
//            //1.没有缓存信息
//            standardCard = paperAnswerCardService.createAnswerCard(paper, subject, userId, terminal);
//        }else if (null == standardCard.getPaper().getQuestions()
//                || standardCard.getPaper().getQuestions().size() != estimatePaper.getQuestions().size()){
//            //2.试卷被更新导致缓存信息有误
//            standardCard = paperAnswerCardService.createAnswerCard(paper, subject, userId, terminal);
//            matchServiceComponent.removeCacheInfo(paperId,subject,userId);
//        }
        StandardCard standardCard = paperAnswerCardService.createAnswerCard(paper, subject, userId, terminal);

        //更新用户模考信息
        int updateMatchUserMeta = updateMatchUserMeta(standardCard);

        if (updateMatchUserMeta != 1) {
            /**
             * 0917 update by zhaoxi
             * 更新userMeta失败，说明已经创建答题卡，返回原有答题卡信息
             */
            userMeta = findMatchUserMeta(userId, paperId);
            practiceId = userMeta.getPracticeId();
            standardCard = (StandardCard) answerCardDao.findById(practiceId);
            standardCard.setIdStr(standardCard.getId() + "");
            standardCard.setCurrentTime(System.currentTimeMillis());
            return standardCard;

        }
        //将答题卡id放入 模考大赛答题卡 set
        addMatchPracticeSet(standardCard);

        //计算剩余时间，结束时间-当前时间
        int remainingTime = estimatePaper.getTime();

        long endTime = estimatePaper.getEndTime();
        long currentTime = System.currentTimeMillis();
        long startTime = estimatePaper.getStartTime();

        if (startTime <= currentTime && currentTime < endTime) {
            remainingTime = (int) ((endTime - currentTime) / 1000);
        }
        standardCard.setRemainingTime(remainingTime);
        paperUserMetaService.addUndoPractice(userId, paperId, standardCard.getId());
        standardCard.setIdStr(standardCard.getId() + "");
        standardCard.setCurrentTime(System.currentTimeMillis());

        return standardCard;
    }

    /**
     * 模考大赛交卷
     * 负责把模考大赛的交卷行为记录到reids中（set）,然后发送答题信息到队列（submit_match_answercard）
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @param uname
     */
    public void submitMatchesAnswers(long practiceId, long userId, List<Answer> answers, int area, String uname) throws BizException {
        //答题卡查询，数据直接来自mongo,无缓存
        AnswerCard answerCard = answerCardDao.findById(practiceId);
        //答题卡记录，在第一次进入考试时创建，交卷时必须保证进入考试过
        if (answerCard == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        int paperId = ((StandardCard) answerCard).getPaper().getId();
        // 埋点使用
        SensorsUtils.setMessage("paperId", paperId);
        logger.info("SensorsUtils setMessage paperId: " + paperId);
        //交卷记录存储，只存在redis中，存储的是practiceId
        String setKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        opsForSet.add(setKey, practiceId + "");

        MatchAnswers matchAnswers = MatchAnswers.builder()
                .answers(answers)
                .practiceId(practiceId)
                .userId(userId)
                .area(area)
                .uname(uname)
                .build();
        //模考大赛交卷后，成绩处理队列
        rabbitTemplate.convertAndSend(SUBMIT_MATCH_ANSWERCARD_MQ, matchAnswers);
    }

    /**
     * 判断当前模考大赛是否结束
     * 已经过了结束时间而且所有答题卡都已经处理
     *
     * @param match
     * @return
     */
    private boolean isCurrentMatchFinish(Match match) {
        String matchPracticeIdSetKey = MatchRedisKeys.getMatchPracticeIdSetKey(match.getPaperId());
        boolean isTimeEnd = System.currentTimeMillis() > match.getEndTime();
        boolean isFinished = false;
        try {
            Long size = redisTemplate.opsForSet().size(matchPracticeIdSetKey);
            if (null != size && size.intValue() == 0) {
                isFinished = true;
            }
        } catch (Exception e) {
            logger.error("matchPracticeIdSetKey={}", matchPracticeIdSetKey);
            e.printStackTrace();
        }
        return isTimeEnd && isFinished;
    }

    /**
     * 判断用户是否交卷
     *
     * @param paperId
     * @param practiceId
     * @return
     */
    private boolean isPracticeSubmit(int paperId, long practiceId) {
        String matchSubmitPracticeIdSetKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        return redisTemplate.opsForSet().isMember(matchSubmitPracticeIdSetKey, practiceId + "");
    }

    /**
     * 判断交卷的试卷是否关联申论考试，如果关联则根据全站和报考地区存储排位分数信息
     *
     * @param standardCard
     */
    public void setRedisMetaWithEssay(StandardCard standardCard) {

        /**
         * update:huangqp
         * 查询当前模考大赛信息当做交卷处理所需的模考大赛，在处理多个模考同时运行的时候会出错
         * 用答题卡id下的模考paper的id作为模考大赛的id会更准确些
         */
        if (standardCard.getPaper() == null) {
            logger.error("match's paper is not exists ？？");
            return;
        }
        Match match = matchDao.findById(standardCard.getPaper().getId());
        if (match == null) {
            logger.error("match  is not exists ？？");
            return;
        }
        int qcount = standardCard.getPaper().getQcount();
        //如果答题卡的试卷和当前模考大赛试卷对应，且申论考试id不为0,则添加用户的分数信息到全站、地区总分排名结合里
        if (standardCard.getPaper().getId() == match.getPaperId() && match.getEssayPaperId() > 0) {
            long paperId = match.getEssayPaperId();
            MatchCardUserMeta userMeta = standardCard.getMatchMeta();
            if (userMeta == null) {
                logger.error("没有用户的统计得分");
                return;
            }
            //全站用户总分记录 zset
            if (qcount != standardCard.getUcount()) {
                String totalSortKey = RedisKeyConstant.getUserPracticeScoreKey(paperId);
                ZSetOperations zSetOperations = redisTemplate.opsForZSet();
                zSetOperations.add(totalSortKey, standardCard.getUserId() + "", standardCard.getScore());
            }
        }
    }

    public Match findCurrentMatch(int id) {
        Match match = matchDao.findById(id);
        return match;
    }

    /**
     * 用户区域排名
     *
     * @param currentMatch
     * @param standardCard
     * @return
     */
    public MatchCardUserMeta findMatchCardUserMetaWithEssay(Match currentMatch, StandardCard standardCard) {
        Paper paper = standardCard.getPaper();
        final int currentPaperId = paper.getId();
        //查询所有已经进行的模考大赛
        List<Match> matchList = matchDao.findAll(currentMatch.getStartTime(), currentMatch.getTag(), standardCard.getSubject());
        matchList.removeIf(i -> i.getEssayPaperId() <= 0);
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        long userId = standardCard.getUserId();

        MatchCardUserMeta userMeta = new MatchCardUserMeta();

        Map<Integer, MatchUserMeta> paperIdMetaMap = getPaperIdMetaMap(matchList, userId);
        //设置用户历次模考大赛成绩曲线图
        userMeta.setScoreLine(getLineWithEssay(matchList, paperIdMetaMap));

        //当前用户的地区排名
        MatchUserMeta currentMeta = findMatchUserMeta(userId, currentPaperId);

        int positionId = currentMeta.getPositionId();
        //用户总分地区排名
        String positionPracticeIdSore = RedisKeyConstant.getMockUserAreaTotalScoreKey(currentMatch.getEssayPaperId(), positionId);
        int count = zSetOperations.size(positionPracticeIdSore).intValue();

        Double positionAverage = 0d;

        Long rank = zSetOperations.reverseRank(positionPracticeIdSore, userId + "");
        if (rank == null) {
            rank = Long.valueOf(count);
        } else {
            rank = rank + 1;
        }


        Double max = 0d;
        if (count != 0) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.reverseRangeWithScores(positionPracticeIdSore, 0, 0);

            if (CollectionUtils.isNotEmpty(withScores)) {
                max = new ArrayList<>(withScores).get(0).getScore();
            }
        }
        BigDecimal bigDecimal = new BigDecimal(positionAverage);
        double pAverage = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        BigDecimal maxDecimal = new BigDecimal(max);
        double pMax = maxDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        userMeta.setPositionAverage(pAverage);
        userMeta.setPositionMax(pMax);
        userMeta.setPositionName(currentMeta.getPositionName());
        userMeta.setPositionRank(rank.intValue());
        userMeta.setPositionCount(count);
        userMeta.setPositionBeatRate(count != 0 ? ((count - rank.intValue()) * 100 / count) : 0);
        userMeta.setPositionId(positionId);

        return userMeta;
    }

    /**
     * 计算用户联合模考大赛的曲线图
     *
     * @param matchList
     * @param paperIdMetaMap
     * @return
     */
    private Line getLineWithEssay(List<Match> matchList, Map<Integer, MatchUserMeta> paperIdMetaMap) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();


        final TreeBasedTable<Long, String, Number> basedTable = TreeBasedTable.create();

        for (Match match : matchList) {
            long essayPaperId = match.getEssayPaperId();
            int paperId = match.getPaperId();
            MatchUserMeta meta = paperIdMetaMap.get(paperId);
            //模考未结束，用户未报名，用户未答题
            if (match.getEssayEndTime() > System.currentTimeMillis() ||
                    meta == null || meta.getPracticeId() < 0) {
                continue;
            }
            //地区联合用户总分key
            String pPracticeIdSoreKey = RedisKeyConstant.getMockUserAreaTotalScoreKey(essayPaperId, meta.getPositionId());
            //用户总分
            Double userScore = zSetOperations.score(pPracticeIdSoreKey, meta.getUserId() + "");

            //没有在排名中
            if (userScore == null) {
                userScore = 0D;
            }
            //全站联合分数key
            String paperTotalSoreWithEssay = RedisKeyConstant.getMockUserTotalScoreKey(essayPaperId);
            //全站联合总分
            String paperSoreWithEssay = RedisKeyConstant.getMockScoreSumKey(essayPaperId);
            //总人数
            Long totalCardCount = zSetOperations.size(paperTotalSoreWithEssay);
            //总分数
            String paperScoreSum = valueOperations.get(paperSoreWithEssay);

            Double average = 0d;
            if (paperScoreSum != null && totalCardCount != 0) {
                average = Double.parseDouble(paperScoreSum) / totalCardCount;
            }
            BigDecimal bigDecimal = new BigDecimal(userScore);
            double dUserScore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            BigDecimal maxDecimal = new BigDecimal(average);
            double dAverage = maxDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            basedTable.put(match.getStartTime(), "模考得分", dUserScore);
            basedTable.put(match.getStartTime(), "全站平均得分", dAverage);

        }

        Line line = table2LineSeries(basedTable);
        return line;
    }

    /**
     * 包含联合考试的模考历史查询
     *
     * @param userId
     * @param tag
     * @param subject
     * @param terminal
     * @param cv
     * @return
     */
    public Object getHistoryWithEssay(long userId, int tag, int subject, int terminal, String cv) {
        Map map = (Map) getHistory(userId, tag, subject, terminal, cv);
        if (map.get("list") != null) {
            List<MatchHistory> list = (List) map.get("list");
            for (MatchHistory history : list) {
                checkMatchName(history);
            }
        }
        return map;
    }

    /**
     * 包含联合考试的模考历史查询
     *
     * @param userId
     * @param tag
     * @param subject
     * @return
     */
    public Object getHistoryWithEssayV62(long userId, int tag, int subject,int terminal,String cv) {
        Map map = (Map) getHistoryV62(userId, tag, subject,terminal,cv);
        if (map.get("list") != null) {
            List<MatchHistory> list = (List) map.get("list");
            for (MatchHistory history : list) {
                checkMatchName(history);
            }
        }
        return map;
    }

    /**
     * 维护一份
     *
     * @param history
     */
    private void checkMatchName(final MatchHistory history) {
        if (history.getEssayPaperId() <= 0) {
            return;
        }
        final String cacheNameKey = RedisKeyConstant.getPracticeInfoKey();
        Object cacheName = coreRedisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.hGet(cacheNameKey.getBytes(), String.valueOf(history.getPaperId()).getBytes());
            }
        });
        if (cacheName == null || !cacheNameKey.equals(history.getName())) {
            logger.info("hash redis writing ……,key={},value={}", history.getPaperId(), history.getName());
            coreRedisTemplate.execute(new RedisCallback() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    redisConnection.hSet(cacheNameKey.getBytes(), String.valueOf(history.getEssayPaperId()).getBytes(), JSONObject.toJSONString(history.getName()).getBytes());
                    return null;
                }
            });
        }
    }

    /**
     * 查询用户
     *
     * @param userId
     * @param practiceId 行测或者申论都传的行测练习id
     * @param tag
     * @return
     * @throws BizException
     */
    public Object getHistoryTag(long userId, long practiceId, int tag) throws BizException {
        //用户是否有行测成绩
        long lineTestId = 0;
        //用户是否有申论成绩
        long essayPaperId = 0;
        //3为省考申论
        if (3 == tag) {
            lineTestId = isScore(userId, practiceId);
            essayPaperId = practiceId;
        } else {
            essayPaperId = isScoreEssay(userId, practiceId);
            lineTestId = practiceId;
        }
        Map map = Maps.newHashMap();
        /* 有行测有申论 联合考试 */
        if (essayPaperId > 0 && lineTestId > 0) {
            map.put("subject", 3);
            map.put("subjectName", "总体");
        } else if (essayPaperId <= 0 && lineTestId > 0) {
            map.put("subject", 1);
            map.put("subjectName", "行测");
        } else if (essayPaperId > 0 && lineTestId <= 0) {
            map.put("subject", 2);
            map.put("subjectName", "申论");
        }
        map.put("practiceId", lineTestId);
        map.put("essayId", essayPaperId);
        return map;
    }

    /**
     * 通过用户答题卡id和用户id,查询是否有申论考试成绩
     *
     * @return
     */
    public long isScoreEssay(long userId, long practiceId) throws BizException {
        AnswerCard card = answerCardDao.findById(practiceId);
        if (card == null || !(card instanceof StandardCard)) {
            logger.error("答题卡信息无效，practiceId ={}", practiceId);
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        Match match = findCurrentMatch(((StandardCard) card).getPaper().getId());
        long essayPaperId = match.getEssayPaperId();
        if (essayPaperId <= 0) {
            return 0;
        }
        if (!hasEssayScore(essayPaperId, userId)) {
            return 0;
        }

        return match.getEssayPaperId();
    }

    /**
     * 判断用户是否有申论id
     *
     * @param essayPaperId
     * @param userId
     * @return
     */
    private boolean hasEssayScore(long essayPaperId, long userId) {
        //申论分数
        String essayPaperScore = RedisKeyConstant.getMockUserTotalScoreKey(essayPaperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Double essayScore = zSetOperations.score(essayPaperScore, userId + "");
        if (essayScore == null || 0 == essayScore.intValue()) {
            return false;
        }
        return true;
    }

    /**
     * 判断用户是否有行测id
     *
     * @param paperId
     * @param userId
     * @return
     */
    private boolean hasLineTestScore(int paperId, long userId) {
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);
        if (userMeta == null) {
            return false;
        }
        long practiceId = userMeta.getPracticeId();
        if (practiceId <= 0) {
            return false;
        }
        String scoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Double score = zSetOperations.score(scoreKey, practiceId + "");
        if (score == null) {
            return false;
        }
        return true;
    }

    /**
     * 通过用户id和申论试卷id,判断是否有关联的行测考试成绩
     *
     * @param userId
     * @param essayPaperId
     */
    public long isScore(long userId, long essayPaperId) {
        String totalScore = RedisKeyConstant.getMockUserTotalScoreKey(essayPaperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Double score = zSetOperations.score(totalScore, userId + "");
        if (score == null || score.intValue() == 0) {
            return 0;
        }
        return getLineTestId(essayPaperId, userId);
    }

    /**
     * 通过用户id，和联合考试的申论id，查询学员行测的答题卡id
     *
     * @param essayPaperId
     * @param userId
     * @return
     */
    private long getLineTestId(long essayPaperId, long userId) {
        return -1;
    }

    public List<Match> getMatchesWithEssay(long userId, int subject) throws BizException {
        StopWatch stopWatch = new StopWatch("getMatchesWithEssay");
        stopWatch.start("findUsefulMatch" + subject);
        List<Match> matches = matchDao.findUsefulMatch(subject);
        stopWatch.stop();
        if (CollectionUtils.isEmpty(matches)) {
            throw new BizException(MatchErrors.NO_MATCH);
        }
        for (Match match : matches) {
            if (0 >= match.getEssayPaperId()) {
                stopWatch.start("packageMatchInfo" + match.getPaperId());
                //只有的行测模考大赛
                packageMatchInfo(match, userId, false);
                stopWatch.stop();
                match.setStage(1);
                /**
                 * 行测模考大赛添加大礼包图片
                 * update by  lzj
                 * update by lizhenjuan 2019-03-18 增加事业单位公基科目估分活动
                 */
                stopWatch.start("addGiftInfoForMatchSearchList");
                if (bigBagUsedSubjectConfig.isEnabledUserSubject(subject)) {
                    logger.info("行测 getIconUrl");
                    match.setIconUrl(paperAnswerCardUtilComponent.addGiftInfoForMatchSearchList(match));
                }
                stopWatch.stop();
            } else {
                //携带申论的模考大赛
                packageMatchInfoWithEssay(match, userId);
                //拼接行测申论考试时间
                packageTimeInfo(match);
                checkMatchName(match);
            }
            stopWatch.start("checkMatchFlag");
            checkMatchFlag(match);
            stopWatch.stop();
            //当状态为未报名和停止报名时，stage 统一置为 0
            if (MatchStatus.UN_ENROLL == match.getStatus() || MatchStatus.PASS_UP_ENROLL == match.getStatus()) {
                match.setStage(0);
            }
            //如果是申论阶段，则将tag换为3
            if (2 == match.getStage()) {
                match.setTag(3);
            }
        }
        if (matchConfig.getEnrollNoAreaSubjectCollection().contains(subject)) {
            matches.forEach(i -> i.setEnrollFlag(1));
        }
        if (stopWatch.getTotalTimeSeconds() > 1) {
            logger.info(stopWatch.prettyPrint());
        }
        return matches;
    }

    public Match getMatchWithEssay(long userId, int subject) throws BizException {
        Match match = matchDao.findCurrentForPc(subject);

        if (match == null) {
            throw new BizException(MatchErrors.NO_MATCH);
        }
        if (0 >= match.getEssayPaperId()) {
            //只有的行测模考大赛
            packageMatchInfo(match, userId, false);
            match.setStage(1);
        } else {
            //携带申论的模考大赛
            packageMatchInfoWithEssay(match, userId);
            //拼接行测申论考试时间
            packageTimeInfo(match);
            checkMatchName(match);
        }
        checkMatchFlag(match);
        //当状态为未报名和停止报名时，stage 统一置为 0
        if (MatchStatus.UN_ENROLL == match.getStatus() || MatchStatus.PASS_UP_ENROLL == match.getStatus()) {
            match.setStage(0);
        }
        //如果是申论阶段，则将tag换为3
        if (2 == match.getStage()) {
            match.setTag(3);
        }
        return match;
    }

    /**
     * 检查缓存中模考大赛名称是否有更改，有的话，更新缓存
     *
     * @param match
     */
    private void checkMatchName(final Match match) {
        if (match.getEssayPaperId() <= 0) {
            return;
        }
        final String cacheNameKey = RedisKeyConstant.getPracticeInfoKey();
        Object cacheName = coreRedisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.hGet(cacheNameKey.getBytes(), String.valueOf(match.getPaperId()).getBytes());
            }
        });
        if (cacheName == null || !cacheNameKey.equals(match.getName())) {
            logger.info("hash redis writing ……,key={},value={}", match.getPaperId(), match.getName());
            coreRedisTemplate.execute(new RedisCallback() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    redisConnection.hSet(cacheNameKey.getBytes(), String.valueOf(match.getPaperId()).getBytes(), JSONObject.toJSONString(match.getName()).getBytes());
                    return null;
                }
            });
        }
    }

    /**
     * 判断用户考试成绩情况(0表示没有成绩报告1表示只有行测报告2只有申论报告3行测申论报告都有)
     *
     * @param match
     */
    private void checkMatchFlag(Match match) {
        //如果用户报名时
        if (match.getUserMeta() == null) {
            match.setFlag(0);
            return;
        }
        if (match.getStage() == 1) {
            if (MatchStatus.REPORT_AVAILABLE == match.getStatus()) {
                match.setFlag(1);
            } else {
                match.setFlag(0);
            }
        } else if (match.getStage() == 2) {
            boolean essayFlag = MatchStatus.REPORT_AVAILABLE == match.getStatus();
            boolean lineTestFlag = hasLineTestScore(match.getPaperId(), match.getUserMeta().getUserId());
            if (essayFlag && lineTestFlag) {
                match.setFlag(3);
            } else if (essayFlag && !lineTestFlag) {
                match.setFlag(2);
            } else if (!essayFlag && lineTestFlag) {
                match.setFlag(1);
            } else {
                match.setFlag(0);
            }
        }
    }

    private void packageTimeInfo(Match match) {
        long startTime = match.getStartTime();
        long endTime = match.getEndTime();
        long essayStartTime = match.getEssayStartTime();
        long essayEndTime = match.getEssayEndTime();
        String timeInfo = "行测" + getTimeInfo(startTime, endTime) + "\n申论" + getTimeInfo(essayStartTime, essayEndTime);
        match.setTimeInfo(timeInfo);
    }

    private String getTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        int day = instance.get(Calendar.DAY_OF_WEEK);

        //考试时间：2017年8月20日（周日）09:00-11:00
        String timeInfo = DateFormatUtils.format(startTime, "yyyy年M月d日") + "（%s）%s-%s";
        String dayString = "";
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
        }

        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "：" + timeInfo;
    }

    /**
     * 从缓存中取用户该次模考大赛的成绩统计(总分报告数据不会发生变动，周期可以大点)
     *
     * @return
     */
    public AnswerCard getMatchReportInRedis(int paperId, long userId) {
        String reportKey = MatchRedisKeys.getLineTestMatchReportKey(paperId, userId);
        Object result = redisTemplate.opsForValue().get(reportKey);
        if (result != null && result instanceof AnswerCard) {
            return (AnswerCard) result;
        }
        return null;
    }


    public List<EstimatePaper> findPastMatches(int tag, long userId, int offset, int size, int subject) {
        /* 分页查询模考试卷 TODO match加缓存 */
        //查询match只是为了获得模考大赛试卷id，其他的无任何作用
        List<Match> pastMatches = matchDao.findPastMatches(tag, offset, size, subject);
        if (subject == SubjectType.GWY_XINGCE && tag == 2) {     //行测省考，过滤掉2019之前的所有
            pastMatches.removeIf(i -> i.getEndTime() <= 1523674800000L);
        }
        //删除考试为结束的考试（考试结束但是还未统计出报告阶段的试卷）
        pastMatches.removeIf(i -> !isCurrentMatchFinish(i));
        List<Integer> paperIds = pastMatches.stream().map(Match::getPaperId).collect(Collectors.toList());
        List<EstimatePaper> papers = paperService.findBathByIds(paperIds).stream().filter(i -> i instanceof EstimatePaper).map(i -> (EstimatePaper) i).collect(Collectors.toList());
        //批量查询meta信息
        final List<PaperUserMeta> paperUserMetas = paperUserMetaService.findBatch(userId, paperIds);
        for (EstimatePaper paper : papers) {//遍历试卷，填充用户做卷信息
            for (PaperUserMeta paperUserMeta : paperUserMetas) {
                if (paperUserMeta.getPaperId() == paper.getId()) {//met信息匹配则设置
                    //设置meta信息
                    paper.setUserMeta(paperUserMeta);
                    paper.setType(PaperType.MATCH_AFTER);
                }
            }
        }
        papers.sort((a, b) -> b.getEndTime() - a.getEndTime() > 0 ? 1 : -1);
        //添加课程ID,参加人数
        List<EstimatePaper> paperResult = getPaperInfo(papers, pastMatches);
        return paperResult;
    }


    /**
     * 含有统计信息的往期模考接口
     *
     * @param tag
     * @param userId
     * @param offset
     * @param size
     * @param subject
     * @return
     */
    public List<PaperVo> findPastMatchesWithStatics(int tag, long userId, int offset, int size, int subject) {
        List<Match> pastMatches = matchDao.findPastMatches(tag, offset, size, subject);
        pastMatches.removeIf(i -> !isCurrentMatchFinish(i));
        List<Integer> paperIds = pastMatches.stream().map(Match::getPaperId).collect(Collectors.toList());
        final List<PaperUserMeta> paperUserMetas = paperUserMetaService.findBatch(userId, paperIds);
        List<PaperVo> paperVos = Lists.newArrayList();
        pastMatches.forEach(item -> {
            PaperVo paperVo = new PaperVo();
            paperVo.setMatchId(item.getPaperId());
            paperVo.setName(item.getName());
            paperVo.setCourseId(item.getCourseId());
            //试卷提交次数统计
            String paperSubmitKey = PaperRedisKeys.getPaperSubmitKey(item.getPaperId(), AnswerCardType.MATCH_AFTER);
            paperVo.setAnswerCount(0);
            try {
                Long count = redisTemplate.opsForSet().size(paperSubmitKey);
                int matchCount = matchServiceComponent.countMatchSubmitSize(item.getPaperId());
                paperVo.setAnswerCount(count.intValue() + matchCount);
            } catch (Exception e) {
                e.printStackTrace();
                redisTemplate.delete(paperSubmitKey);
            }
            //用户试卷做题次数和当前状态
            Optional<PaperUserMeta> first = paperUserMetas.stream().filter(i -> i.getPaperId() == item.getPaperId()).findFirst();
            if (first.isPresent()) {
                PaperUserMeta paperUserMeta = first.get();
                long currentPracticeId = paperUserMeta.getCurrentPracticeId();
                paperVo.setPracticeId(currentPracticeId + "");
                if (currentPracticeId == -1L) {
                    paperVo.setAnswerStatus(1);
                } else {
                    paperVo.setAnswerStatus(0);
                }
                List<Long> practiceIds = paperUserMeta.getPracticeIds();
                if (CollectionUtils.isEmpty(practiceIds)) {
                    paperVo.setCompleteCount(0);
                } else {
                    long count = practiceIds.stream()
                            .filter(i -> i.longValue() != currentPracticeId)
                            .count();
                    logger.info("ids.size={},count={},NumberUtils={}", practiceIds.size(), count, NumberUtils.convertNumberToTargetClass(count, Integer.class));
                    paperVo.setCompleteCount(NumberUtils.convertNumberToTargetClass(count, Integer.class));
                }
            } else {
                paperVo.setPracticeId("-1");
                paperVo.setAnswerStatus(1);
            }
            paperVos.add(paperVo);
        });
        return paperVos;
    }


    public long findPastMatchesTotal(int tag, int subject) {
        return matchDao.findPastMatchesTotal(tag, subject);
    }

    /**
     * overwrite eroll
     */
    public void enrollV3(int paperId, long userId, int positionId, Long schoolId, String schoolName) throws BizException {
        logger.info("match enroll2 paperId={},userId={},positionId={},schoolName={}", paperId, userId, positionId, schoolId, schoolName);

        Match match = matchDao.findById(paperId);
        if (match == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        enrollHelpV3(paperId, userId, positionId, match, schoolId, schoolName);
    }

    /**
     * overwrite enrollHelp
     * set schoolName and schoolId
     */
    private void enrollHelpV3(int paperId, long userId, int positionId, Match match, Long schoolId, String schoolName) throws BizException {
        //模考大赛考试开始后，不能报名
        if (System.currentTimeMillis() > match.getStartTime()) {
            throw new BizException(MatchErrors.MISSING_MATCH);
        }

        /*获取用户报名信息*/
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);

        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();


        //第一次报名，报名人数加1
        if (userMeta == null) {
            userMeta = MatchUserMeta.builder()
                    .id(getMatchUserMetaId(userId, paperId))
                    .userId(userId)
                    .positionId(positionId)
                    .positionName(PositionConstants.getFullPositionName(positionId))
                    .practiceId(-1)
                    .paperId(paperId)
                    .schoolId(schoolId)
                    .schoolName(schoolName)
                    .build();
            String positionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, positionId);
            opsForSet.add(positionEnrollSetKey, userId + "");
            /*  该模考报名总数  */
            String countKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
            opsForValue.increment(countKey, 1);

            //更改报名地区，只更新地区不加人数
        } else {
            updateUserMetaPositionInfoV3(opsForSet, userMeta, positionId, paperId, userId, schoolId, schoolName);
        }
        matchDao.saveUserMeta(userMeta);
    }

    /**
     * invoke old updateUserMetaPositionInfo and add new logic
     * remove old and set new message about user's schoolId
     */
    private void updateUserMetaPositionInfoV3(SetOperations<String, String> opsForSet, MatchUserMeta userMeta, int newPositionId, int paperId, long userId, Long schoolId, String schoolName) {
        updateUserMetaPositionInfo(opsForSet, userMeta, newPositionId, paperId, userId);
        Long oldSchoolId = userMeta.getSchoolId();
        userMeta.setSchoolName(schoolName);
        userMeta.setSchoolId(schoolId);
        String oldSchoolIdSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, oldSchoolId.intValue());
        String schoolIdSetKey = MatchRedisKeys.getSchoolEnrollSetKey(paperId, schoolId);
        opsForSet.remove(oldSchoolIdSetKey, userId + "");
        opsForSet.add(schoolIdSetKey, userId + "");
    }

    public Match getMatchMachineMatchV3(long userId, int subject) throws BizException {
        Match match = matchDao.findCurrentForPc(subject);
        if (match == null) {
            throw new BizException(MatchErrors.NO_MATCH);
        }
        handlerUserMatch(match, userId);

        return match;
    }

    /**
     * 模考大赛数据补充用户信息
     *
     * @param match
     * @param userId
     */
    private void handlerUserMatch(Match match, long userId) {
        int paperId = match.getPaperId();
        long startTime = match.getStartTime();
        long endTime = match.getEndTime();
        long currentTime = System.currentTimeMillis();
        //get user machine match information
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);
        // already sign up
        if (userMeta != null) {
            // generate
            String setKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, userMeta.getPositionId());
            SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
            Long positionCount = opsForSet.size(setKey);
            userMeta.setPositionCount(positionCount.intValue());
            //Get the answer sheet to determine whether to start answer the match
            long practiceId = userMeta.getPracticeId();
            if (practiceId > 0) {
                // Get the boolean to judge whether submit
                boolean isSubmit = isPracticeSubmit(paperId, practiceId);
                // check the current time whether time out
                boolean currentFinish = isCurrentMatchFinish(match);
                // already answer
                if (isSubmit) {
                    match.setStatus(currentFinish ? MatchStatus.REPORT_AVAILABLE : MatchStatus.REPORT_UNAVILABLE);

                } else if (currentTime < endTime) {
                    match.setStatus(MatchStatus.NOT_SUBMIT);
                } else {
                    match.setStatus(MatchStatus.REPORT_UNAVILABLE);
                }
            } else {

                if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(60)) {
                    //距开始大于一个小时
                    match.setStatus(MatchStatus.ENROLL);
                } else if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(10)) {
                    //距开始小于一个小时,大于10分钟
                    match.setStatus(MatchStatus.START_UNAVILABLE);
                } else if (startTime >= currentTime) {
                    //距开始时间小于当前时间
                    match.setStatus(MatchStatus.START_AVILABLE);
                } else {
                    //已经开始
                    match.setStatus(MatchStatus.MATCH_UNAVILABLE);
                }
            }
        } else {
            match.setStatus(MatchStatus.UN_ENROLL);
            /**
             * @update 2018/08/20 huangqp
             * 前提:未报名
             * 原逻辑：已经开始30分钟或者考试已结束，如果是联合考试，置状态为9，否则为5
             * 现逻辑：已经开始30分钟或者考试已结束，状态都置为9
             */
            //已经开始30分钟或者考试已结束
            if (currentTime > startTime) {
                //状态置为“未报名且错过报名”
                match.setStatus(MatchStatus.PASS_UP_ENROLL);
            }
        }
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        /* 返回报名总数*/
        String enrollCountStr = opsForValue.get(MatchRedisKeys.getTotalEnrollCountKey(paperId));

        int enrollCount = 0;
        if (enrollCountStr != null) {
            enrollCount = Integer.valueOf(enrollCountStr);
        }

        match.setEnrollCount(enrollCount);
        match.setUserMeta(userMeta);
    }

    /***
     * 拼接课程ID，参加考试人数
     * @param papers
     * @param pastMatches
     * @return
     */
    public List<EstimatePaper> getPaperInfo(List<EstimatePaper> papers, List<Match> pastMatches) {
        if (CollectionUtils.isEmpty(papers)) {
            return Lists.newArrayList();
        }

        for (EstimatePaper paper : papers) {
            //参加考试的人数
            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paper.getId());
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            Long takePartUserCount = zSetOperations.size(paperPracticeIdSore);
            paper.setTakePartInCount(takePartUserCount.intValue());
            //课程ID
            for (Match match : pastMatches) {
                if (paper.getId() == match.getPaperId()) {
                    paper.setCourseId(match.getCourseId());
                }
            }
        }
        return papers;
    }

    /**
     * 招警机考专用模考大赛首页列表查询
     *
     * @param userId
     * @param subjectId
     * @return
     */
    public List<Match> getMatchMachineMatchV4(long userId, int subjectId) {
        List<Match> matches = matchDao.findListByPc(subjectId);
        if (userId > 0) {
            matches.stream().forEach(i -> {
                handlerUserMatch(i, userId);
            });
        } else {
            matches.stream().forEach(this::initUserMeta);
        }
        return matches;
    }

    /**
     * 初始化首页按钮状态和报名人数
     *
     * @param match
     */
    private void initUserMeta(Match match) {
        int paperId = match.getPaperId();
        long startTime = match.getStartTime();
        long currentTime = System.currentTimeMillis();
        match.setStatus(MatchStatus.UN_ENROLL);
        if (currentTime > startTime) {
            //状态置为“未报名且错过报名”
            match.setStatus(MatchStatus.PASS_UP_ENROLL);
        }
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        /* 返回报名总数*/
        String enrollCountStr = opsForValue.get(MatchRedisKeys.getTotalEnrollCountKey(paperId));
        int enrollCount = 0;
        if (enrollCountStr != null) {
            enrollCount = Integer.valueOf(enrollCountStr);
        }
        match.setEnrollCount(enrollCount);
    }

    /**
     * 判断模考大赛新旧逻辑状态
     *
     * @param terminal
     * @param subjectId
     * @param cv
     * @return false使用新模考大赛，true使用旧模考大赛
     */
    public Object getMatchOldFlag(int terminal, int subjectId, String cv) {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("matchOldFlag", true);  //默认使用旧模板
        //版本号判断
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {

            if (VersionUtil.compare(cv, matchChangeConfig.getMatchAndroidCvDeadline()) < 0) {      //版本低,直接返回默认值
                return map;
            }
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            if (VersionUtil.compare(cv, matchChangeConfig.getMatchIphoneCvDeadline()) < 0) {       //版本低,直接返回默认值
                return map;
            }
        }
        map.put("matchOldFlag", isOldFlagForSubject(subjectId));
        return map;
    }

    /**
     * 科目是否是旧模考版本
     *
     * @param subjectId
     * @return
     */
    public boolean isOldFlagForSubject(int subjectId) {
        Function<String, List<Integer>> convert = (str -> {
            if (StringUtils.isNotBlank(str)) {
                String[] split = str.split(",");
                List<Integer> collect = Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());
                return collect;
            }
            return Lists.newArrayList();
        });
        //科目判断
        if (Boolean.parseBoolean(matchChangeConfig.getDefaultMatchOldFlag())) {   //默认是旧的
            final List<Integer> subjectIds = convert.apply(matchChangeConfig.getMatchNewSubject());  //支持新模考大赛的科目
            if (subjectIds.contains(subjectId)) {
                return false;
            } else {
                return true;
            }
        } else {
            final List<Integer> subjectIds = convert.apply(matchChangeConfig.getMatchOldSubject());  //支持旧模考的科目
            if (subjectIds.contains(subjectId)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 模考大赛首页是否降级
     *
     * @return
     */
    public boolean isDegrade() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int degradeHour = Integer.parseInt(matchChangeConfig.getDegradeHour().trim());
        int degradeMinute = Integer.parseInt(matchChangeConfig.getDegradeMinute().trim());
        int preHour = degradeHour;          //降级前一分钟
        int preMinute = degradeMinute - 1;      //降级前一分钟
        if (preMinute < 0) {
            preHour = degradeHour - 1;
            preMinute = 59;
        }
        boolean flag = (hour == preHour && minute == preMinute) || (hour == degradeHour && minute == degradeMinute);
        return Boolean.parseBoolean(matchChangeConfig.getDegrade()) && flag;
    }

    /**
     * v3/v4首页对应的降级逻辑
     *
     * @param userId
     * @param subject
     * @return
     */
    public Object getMatchHeaderMock(long userId, int subject) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //模考大赛基础信息
        String matchKey = MatchRedisKeys.getMatchInfoForDegradeKey(subject);
        List<Match> list = MATCH_HEADER_MOCK_CACHE.getIfPresent(matchKey);
        List<Match> result = Lists.newArrayList();
        try {
            if (null != list) {
                //有缓存
                if (!list.isEmpty()) {
                    //缓存有值
                    result.addAll(list);
                }
            } else {
                logger.info("模考大赛降级逻辑，穿透查询：userId={},subjectId={}", userId, subject);
                List<Match> matches = matchDao.findUsefulMatch(subject);
                if (CollectionUtils.isNotEmpty(matches)) {
                    result.addAll(matches);
                    fillUserMeta(result, userId);
                }
                MATCH_HEADER_MOCK_CACHE.put(matchKey, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        logger.info("模考大赛降级逻辑耗时:{},userId={},subjectId={}", stopWatch.getTotalTimeMillis(), userId, subject);
        return result;
    }

    private void fillUserMeta(List<Match> result, long userId) {
        //添加报名信息和用户状态
        for (Match match : result) {
            int matchId = match.getPaperId();
            MatchUserMeta userMeta = MatchUserMeta.builder().userId(userId).paperId(matchId)
                    .id(getMatchUserMetaId(userId, matchId))
                    .positionCount(1)
                    .positionId(1)
                    .positionName("北京")
                    .practiceId(-1L)
                    .build();
            long startTime = match.getStartTime();
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis < startTime + TimeUnit.MINUTES.toMillis(30)) {
                match.setStatus(MatchStatus.ENROLL);
            } else {
                match.setStatus(MatchStatus.REPORT_UNAVILABLE);
                userMeta.setPracticeId(1111111L);
            }
            //总报名数
            ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
            /* 返回报名总数*/
            String enrollCountStr = opsForValue.get(MatchRedisKeys.getTotalEnrollCountKey(matchId));
            int enrollCount = 0;
            if (enrollCountStr != null) {
                enrollCount = Integer.valueOf(enrollCountStr);
            }
            match.setEnrollCount(enrollCount);
            match.setUserMeta(userMeta);
            match.setStage(1);
        }
    }

    /**
     * v2模考大赛首页降级逻辑
     *
     * @param userId
     * @param subject
     * @return
     * @throws BizException
     */
    public Object getMatchHeaderMockPc(long userId, int subject) throws BizException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //模考大赛基础信息
        String matchKey = MatchRedisKeys.getPcMatchInfoForDegradeKey(subject);
        List<Match> list = MATCH_HEADER_MOCK_CACHE.getIfPresent(matchKey);
        Match result = null;
        try {
            if (null != list) {
                //有缓存
                if (!list.isEmpty()) {
                    //缓存有值
                    result = list.get(0);
                }
            } else {
                //没缓存
                logger.info("模考大赛降级逻辑，穿透查询：userId={},subjectId={}", userId, subject);
                result = matchDao.findCurrentForPc(subject);
                ArrayList<Match> matches = Lists.newArrayList();
                if (null != result) {
                    matches.add(result);
                    fillUserMeta(matches, userId);
                }
                MATCH_HEADER_MOCK_CACHE.put(matchKey, matches);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        logger.info("模考大赛降级逻辑耗时:{},userId={},subjectId={}", stopWatch.getTotalTimeMillis(), userId, subject);
        if (null == result) {
            throw new BizException(MatchErrors.NO_MATCH);
        }
        return result;
    }

    public Double getMatchScore(Integer matchId) {
        Double score = MATCH_SCORE_CACHE.getIfPresent(matchId);
        if (null == score) {
            Paper paper = paperDao.findById(matchId);
            if (null != paper) {
                score = new Double(paper.getScore());
            }
            if (null != score) {
                MATCH_SCORE_CACHE.put(matchId, score);
            }
        }
        return score;
    }
}
