//package com.huatu.tiku.match.service.impl.v1.meta;
//
//import com.google.common.collect.Maps;
//import com.huatu.common.exception.BizException;
//import com.huatu.tiku.common.bean.AreaConstants;
//import com.huatu.tiku.match.bean.entity.MatchEssayUserMeta;
//import com.huatu.tiku.match.bean.entity.MatchUserMeta;
//import com.huatu.tiku.match.common.MatchInfoRedisKeys;
//import com.huatu.tiku.match.constant.MatchErrors;
//import com.huatu.tiku.match.enums.EssayMatchStatusEnum;
//import com.huatu.tiku.match.enums.MatchInfoEnum;
//import com.huatu.tiku.match.service.v1.meta.MatchEssayUserMetaService;
//import com.huatu.tiku.match.web.event.MatchEssayUserChangeEvent;
//import com.huatu.ztk.paper.common.RedisKeyConstant;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.collections.MapUtils;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.ApplicationListener;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SetOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.stereotype.Service;
//import service.impl.BaseServiceHelperImpl;
//import tk.mybatis.mapper.entity.Example;
//
//import java.sql.Timestamp;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by huangqingpeng on 2018/10/17.
// */
//@Slf4j
//@Service
//public class MatchEssayUserMetaServiceImpl extends BaseServiceHelperImpl<MatchEssayUserMeta> implements MatchEssayUserMetaService, ApplicationContextAware, ApplicationListener<MatchEssayUserChangeEvent> {
//    public MatchEssayUserMetaServiceImpl() {
//        super(MatchEssayUserMeta.class);
//    }
//
//    @Autowired
//    @Qualifier("redisTemplateWithoutServerName")
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private ApplicationContext applicationContext;
//    private static final String REDIS_DEFAULT_KEY = "0";
//    private static final String REDIS_DEFAULT_FIELD = "0";
//    private static final String REDIS_DEFAULT_VALUE = "-1";
//    /**
//     * HASH缓存中的fields
//     */
//    private static final String MATCH_USER_ENROLL_INFO_POSITION_ID = "positionId";
//    private static final String MATCH_USER_ENROLL_INFO_POSITION_NAME = "positionName";
//    private static final String MATCH_USER_ENROLL_INFO_ENROLL_TIME = "enrollTime";
//    private static final String MATCH_USER_ENROLL_INFO_PRACTICE_ID = "practiceId";
//    private static final String MATCH_USER_ENROLL_INFO_CARDCREATE_TIME = "cardCreateTime";
//    private static final String MATCH_USER_ENROLL_INFO_IS_ANSWER = "isAnswer";
//    private static final String MATCH_USER_ENROLL_INFO_SUBMIT_TIME = "submitTime";
//    private static final String MATCH_USER_ENROLL_INFO_SUBMIT_TYPE = "submitType";
//    private static final String MATCH_USER_ENROLL_INFO_SCORE = "score";
//    private static final String MATCH_USER_ENROLL_INFO_ESSAY_PAPERID = "essayPaperId";
//
//    /**
//     * 读取用户状态缓存
//     * 缓存状态滞后存在的问题：新后台的按钮变动滞后，所以需要一个轻量的异步消息队列可以及时处理缓存添加
//     *
//     * @param essayPaperId
//     * @param userId
//     * @return
//     */
//    @Override
//    public EssayMatchStatusEnum getEssayUserAnswerStatus(long essayPaperId, int userId) {
//        String userAnswerStatusKey = MatchInfoRedisKeys.getEssayUserAnswerStatusKey(essayPaperId);
//        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        String status = hashOperations.get(userAnswerStatusKey, String.valueOf(userId));
//
//        if (null != status) {
//            return EssayMatchStatusEnum.create(Integer.parseInt(status));
//
//        }
//        return EssayMatchStatusEnum.DEFAULT;
//    }
//
//    @Override
//    public boolean isFinished(long essayPaperId) {
//        String setKey = RedisKeyConstant.getPublicUserSetPrefix(essayPaperId);
//        SetOperations setOperations = redisTemplate.opsForSet();
//        Long size = setOperations.size(setKey);
//        if (null != size && size > 0) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public int saveMatchEnrollInfo(int userId, long essayPaperId, int positionId) {
//        return saveMatchEnrollInfo(userId, essayPaperId, positionId, -1);
//    }
//
//    @Override
//    public int saveMatchEnrollInfo(int userId, long essayPaperId, int positionId, long enrollTime) {
//        boolean fromFlag = enrollTime == -1;    //是否通过新系统报名（如果是，则同步到旧系统，否则只添加，不同步）
//        long current = fromFlag ? System.currentTimeMillis() : enrollTime;  //报名时间(新系统报名使用当前时间，老系统报名使用传入的时间)
//        //TODO 报名地区名称通过文件加载得到，而非数据库查询得到
//        String positionName = AreaConstants.getFullAreaNmae(positionId);
//        //加入统计表
//        MatchEssayUserMeta matchEssayUserMeta = MatchEssayUserMeta.builder()
//                .essayPaperId(essayPaperId)
//                .userId(userId)
//                .positionId(positionId)
//                .positionName(positionName)
//                .enrollTime(new Timestamp(current))
//                .build();
//        //加入缓存
//        int i = putEnrollInfo2Cache(matchEssayUserMeta);
//        boolean isNewCreate = i > 0; //i标识被存储的报名信息量（1标识添加0标识修改）
//        if (isNewCreate) {
//            insert(matchEssayUserMeta);
//            return i;
//        }
//        MatchEssayUserChangeEvent.OperationEnum operationEnum = isNewCreate ? MatchEssayUserChangeEvent.OperationEnum.INSERT : MatchEssayUserChangeEvent.OperationEnum.UPDATE;
//        MatchEssayUserChangeEvent matchUserChangeEvent = new MatchEssayUserChangeEvent(applicationContext, matchEssayUserMeta, operationEnum);
//        applicationContext.publishEvent(matchUserChangeEvent);
//
////        if(fromFlag){//同步报名信息到paper项目
////            rabbitTemplate.convertAndSend("", RabbitMatchKeyConstant.MatchEssayEnrollInfo,matchUserMeta);
////        }
//        return i;
//    }
//
//
//    /**
//     * 创建答题卡数据同步
//     *
//     * @param essayPaperId
//     * @param userId
//     * @param practiceId
//     * @param createTime
//     */
//    @Override
//    public void savePracticeId(long essayPaperId, int userId, Long practiceId, Long createTime) throws BizException {
//        boolean fromFlag = createTime == -1; //是否通过新系统创建的答题卡（如果是，则同步到旧系统，否则只添加，不同步）
//        long current = fromFlag ? System.currentTimeMillis() : createTime;
//        MatchEssayUserMeta matchUserEnrollInfo = findMatchEssayUserEnrollInfo(userId, essayPaperId);
//        if (null == matchUserEnrollInfo) {
//            throw new BizException(MatchErrors.NO_ENROLLINFO);
//        }
//        /**
//         * 缓存维护
//         */
//        String userEnrollHashKey = MatchInfoRedisKeys.getUserEssayEnrollHashKey(essayPaperId, userId);
//        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
//        Map<String, Object> map = Maps.newHashMap();
//        map.put(MATCH_USER_ENROLL_INFO_PRACTICE_ID, practiceId);
//        map.put(MATCH_USER_ENROLL_INFO_CARDCREATE_TIME, current);
//        map.put(MATCH_USER_ENROLL_INFO_IS_ANSWER, MatchInfoEnum.AnswerStatus.NO_SUBMIT);
//        hashOperations.putAll(userEnrollHashKey, map);
//        /**
//         * 创建答题卡ID和时间存储
//         */
//        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(essayPaperId);
//        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
//        zSetOperations.add(matchPracticeIdSetKey, practiceId + "", current);
//        //TODO 通过事件队列实现
//        MatchEssayUserMeta matchUserMeta = MatchEssayUserMeta.builder()
//                .essayPaperId(essayPaperId)
//                .userId(userId)
//                .practiceId(practiceId)
//                .cardCreateTime(new Timestamp(current))
//                .isAnswer(MatchInfoEnum.AnswerStatus.NO_SUBMIT.getKey())
//                .build();
//        MatchEssayUserChangeEvent matchUserChangeEvent = new MatchEssayUserChangeEvent(applicationContext, matchUserMeta, MatchEssayUserChangeEvent.OperationEnum.UPDATE);
//        applicationContext.publishEvent(matchUserChangeEvent);
////        //TODO 缓存更新（用户创建答题卡相关）
////        if (fromFlag) {       //创建答题卡的操作在报名数据中实现
////            rabbitTemplate.convertAndSend("", RabbitMatchKeyConstant.MatchEssayCreatePracticeId, matchUserMeta);
////        }
//    }
//
//    /**
//     * 提交答题卡数据同步
//     *
//     * @param essayPaperId
//     * @param userId
//     * @param submitTypeEnum
//     * @param score
//     * @param submitTime
//     * @return
//     * @throws BizException
//     */
//    @Override
//    public int saveMatchScore(long essayPaperId, int userId, MatchInfoEnum.SubmitTypeEnum submitTypeEnum, double score, long submitTime) throws BizException {
//        MatchEssayUserMeta matchUserEnrollInfo = findMatchEssayUserEnrollInfo(userId, essayPaperId);
//        if (null == matchUserEnrollInfo) {
//            throw new BizException(MatchErrors.NO_ENROLLINFO);
//        }
//        Long practiceId = matchUserEnrollInfo.getPracticeId();
//        if (null == practiceId || practiceId < 0) {
//            throw new BizException(MatchErrors.NO_PRACTICEID);
//        }
//        /**
//         * 删除用户答题卡预存zset
//         */
//        String matchPracticeIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(essayPaperId);
//        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
//        zSetOperations.remove(matchPracticeIdSetKey, practiceId + "");
//        boolean fromFlag = submitTime == -1; //提交答题卡时间（如果是，则同步到旧系统，否则只添加，不同步）
//        long current = fromFlag ? System.currentTimeMillis() : submitTime;
//        /**
//         * 缓存维护
//         */
//        String userEnrollHashKey = MatchInfoRedisKeys.getUserEssayEnrollHashKey(essayPaperId, userId);
//        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
//        Map<String, Object> map = Maps.newHashMap();
//        map.put(MATCH_USER_ENROLL_INFO_SCORE, score);
//        map.put(MATCH_USER_ENROLL_INFO_SUBMIT_TYPE, submitTypeEnum.getKey());
//        map.put(MATCH_USER_ENROLL_INFO_SUBMIT_TIME, current);
//        map.put(MATCH_USER_ENROLL_INFO_IS_ANSWER, MatchInfoEnum.AnswerStatus.SUBMIT.getKey());
//        hashOperations.putAll(userEnrollHashKey, map);
//
//        //TODO 通过事件队列实现
//        MatchEssayUserMeta matchEssayUserMeta = MatchEssayUserMeta.builder()
//                .essayPaperId(essayPaperId)
//                .userId(userId)
//                .score(score)
//                .submitTime(new Timestamp(current))
//                .submitType(submitTypeEnum.getKey())
//                .isAnswer(MatchInfoEnum.AnswerStatus.SUBMIT.getKey())
//                .build();
//        MatchEssayUserChangeEvent matchUserChangeEvent = new MatchEssayUserChangeEvent(applicationContext, matchEssayUserMeta, MatchEssayUserChangeEvent.OperationEnum.UPDATE);
//        applicationContext.publishEvent(matchUserChangeEvent);
////        //TODO 缓存更新（用户交卷数据同步）
////        if (fromFlag) {       //交卷的操作在报名数据中实现
////            rabbitTemplate.convertAndSend("", RabbitMatchKeyConstant.MatchEnrollInfo, matchUserMeta);
////        }
//        return 1;
//    }
//
//    private int putEnrollInfo2Cache(MatchEssayUserMeta matchUserMeta) {
//        if (null == matchUserMeta) {
//            return 0;
//        }
//        //用户报名地区缓存
//        saveEnrollHashCache(matchUserMeta);
//        MatchEssayUserMeta matchEssayUserMeta = findMatchEssayUserEnrollInfo(matchUserMeta.getUserId(), matchUserMeta.getEssayPaperId());
//        if (null != matchEssayUserMeta) {
//            return 1;
//        }
//        return 0;
//    }
//
//    /**
//     * 查询申论考试数据
//     *
//     * @param userId
//     * @param essayPaperId
//     * @return
//     */
//    private MatchEssayUserMeta findMatchEssayUserEnrollInfo(Integer userId, Long essayPaperId) {
//        //查询报名数据通过缓存
//        MatchEssayUserMeta matchEssayUserMeta = findEssayEnrollInfoByCache(userId, essayPaperId);
//        if (null != matchEssayUserMeta) {
//            return matchEssayUserMeta;
//        }
//        Example example = new Example(MatchEssayUserMeta.class);
//        example.and().andEqualTo("essayPaperId", essayPaperId).andEqualTo("userId", userId);
//        List<MatchEssayUserMeta> matchUserMetas = selectByExample(example);
//        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
//            matchEssayUserMeta = matchUserMetas.get(0);
//            //添加考试信息到缓存
//            addEnrollInfo2Cache(matchEssayUserMeta);
//            //更新用户报名地区
//            saveEnrollHashCache(matchEssayUserMeta);
//        }
//        return matchEssayUserMeta;
//    }
//
//    /**
//     * reids查询用户申论考试信息
//     *
//     * @param userId
//     * @param essayPaperId
//     * @return
//     */
//    private MatchEssayUserMeta findEssayEnrollInfoByCache(Integer userId, Long essayPaperId) {
//        String userEnrollKey = MatchInfoRedisKeys.getUserEssayEnrollHashKey(essayPaperId, userId);
//        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
//        Map<String, Object> entries = hashOperations.entries(userEnrollKey);
//        if (null == entries || entries.size() == 0) {
//            return null;
//        }
//        try {
//            MatchEssayUserMeta matchEssayUserMeta = convertEnrollMap2Object(entries);
//            return matchEssayUserMeta;
//        } catch (Exception e) {
//            redisTemplate.delete(userEnrollKey);
//            log.error("json parse error,enrollInfo = {}", entries);
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private MatchEssayUserMeta convertEnrollMap2Object(Map<String, Object> enrollMap) {
//        MatchEssayUserMeta matchUserMeta = MatchEssayUserMeta.builder()
//                .positionId(MapUtils.getInteger(enrollMap, MATCH_USER_ENROLL_INFO_POSITION_ID))
//                .positionName(MapUtils.getString(enrollMap, MATCH_USER_ENROLL_INFO_POSITION_NAME))
//                .enrollTime(new Timestamp(MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_ENROLL_TIME)))
//                .practiceId(MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_PRACTICE_ID, -1L))
//                .isAnswer(MapUtils.getInteger(enrollMap, MATCH_USER_ENROLL_INFO_IS_ANSWER, MatchInfoEnum.AnswerStatus.NO_JOIN.getKey()))
//                .submitType(MapUtils.getInteger(enrollMap, MATCH_USER_ENROLL_INFO_SUBMIT_TYPE, MatchInfoEnum.SubmitTypeEnum.NO_SUBMIT.getKey()))
//                .score(MapUtils.getDouble(enrollMap, MATCH_USER_ENROLL_INFO_SCORE, 0D))
//                .essayPaperId(MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_ESSAY_PAPERID, -1L))
//                .build();
//        Long cardCreateTime = MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_CARDCREATE_TIME, -1L);
//        if (cardCreateTime > 0) {
//            matchUserMeta.setCardCreateTime(new Timestamp(cardCreateTime));
//        }
//        Long submitTime = MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_SUBMIT_TIME, -1L);
//        if (submitTime > 0) {
//            matchUserMeta.setSubmitTime(new Timestamp(MapUtils.getLong(enrollMap, MATCH_USER_ENROLL_INFO_ENROLL_TIME)));
//        }
//        return matchUserMeta;
//    }
//
//    private void saveEnrollHashCache(MatchEssayUserMeta matchUserMeta) {
//        String mockUserAreaPrefix = RedisKeyConstant.getMockUserAreaPrefix(matchUserMeta.getEssayPaperId());
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        hashOperations.put(mockUserAreaPrefix, String.valueOf(matchUserMeta.getUserId()), String.valueOf(matchUserMeta.getPositionId()));
//    }
//
//    private void addEnrollInfo2Cache(MatchEssayUserMeta matchUserMeta) {
//        String userEnrollKey = MatchInfoRedisKeys.getUserEssayEnrollHashKey(matchUserMeta.getEssayPaperId(), matchUserMeta.getUserId());
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        HashMap<String, Object> enrollMap = Maps.newHashMap();
//        enrollMap.putAll(convertEnrollObject2Map(matchUserMeta));
//        hashOperations.putAll(userEnrollKey, enrollMap);
//        redisTemplate.expire(userEnrollKey, 30, TimeUnit.DAYS);
//    }
//
//    private Map<String, Object> convertEnrollObject2Map(MatchEssayUserMeta matchUserMeta) {
//        Map<String, Object> enrollMap = Maps.newHashMap();
//        enrollMap.put(MATCH_USER_ENROLL_INFO_POSITION_ID, matchUserMeta.getPositionId());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_POSITION_NAME, matchUserMeta.getPositionName());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_ENROLL_TIME, matchUserMeta.getEnrollTime().getTime());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_PRACTICE_ID, matchUserMeta.getPracticeId());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_IS_ANSWER, matchUserMeta.getIsAnswer());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_SUBMIT_TYPE, matchUserMeta.getSubmitType());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_SCORE, matchUserMeta.getScore());
//        enrollMap.put(MATCH_USER_ENROLL_INFO_ESSAY_PAPERID, matchUserMeta.getEssayPaperId());
//        if (null != matchUserMeta.getCardCreateTime()) {
//            enrollMap.put(MATCH_USER_ENROLL_INFO_CARDCREATE_TIME, matchUserMeta.getCardCreateTime().getTime());
//        }
//        if (null != matchUserMeta.getSubmitTime()) {
//            enrollMap.put(MATCH_USER_ENROLL_INFO_SUBMIT_TIME, matchUserMeta.getSubmitTime().getTime());
//        }
//        return enrollMap;
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public void onApplicationEvent(MatchEssayUserChangeEvent matchEssayUserChangeEvent) {
//        MatchEssayUserChangeEvent.OperationEnum operationEnum = matchEssayUserChangeEvent.getOperationEnum();
//        MatchEssayUserMeta matchEssayUserMeta = matchEssayUserChangeEvent.getMatchEssayUserMeta();
//        if(null == matchEssayUserChangeEvent){
//            return;
//        }
//        switch (operationEnum){
//            case INSERT:
//                insert(matchEssayUserChangeEvent.getMatchEssayUserMeta());
//                break;
//            case UPDATE:
//                Example updateExample = new Example(MatchUserMeta.class);
//                updateExample.and().andEqualTo("essayPaperId", matchEssayUserMeta.getEssayPaperId()).andEqualTo("userId", matchEssayUserMeta.getUserId());
//                updateByExample(matchEssayUserMeta, updateExample);
//                break;
//            default:
//                log.error("event handler error, event = {}",matchEssayUserChangeEvent.getOperationEnum().getKey());
//        }
//    }
//}
