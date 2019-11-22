package com.huatu.ztk.knowledge.controller;

import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.share_type;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_answer_duration;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_correct_rate;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_first_cate;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_id;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_second_cate;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_third_cate;
import static com.huatu.ztk.knowledge.common.analysis.model.EventType.ShareTest.test_type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionDataHandleState;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.analysis.annotation.AnalysisReport;
import com.huatu.ztk.knowledge.common.analysis.event.AnalysisEvent;
import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;
import com.huatu.ztk.knowledge.common.analysis.model.EventType;
import com.huatu.ztk.knowledge.common.analysis.publisher.SpringContextPublisher;
import com.huatu.ztk.knowledge.daoPandora.KnowledgeMapper;
import com.huatu.ztk.knowledge.service.PoxyUtilService;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;


/**
 * @author zhengyi
 * @date 2018-12-22 19:15
 **/
@RestController
@RequestMapping("/analy")
@Slf4j
public class TestController {

    @Autowired
    PoxyUtilService poxyUtilService;
    private final SpringContextPublisher springContextPublisher;
    
    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPersistenceDao questionPersistenceDao;

    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;
    
    @Autowired
    private QuestionDataHandleState dataHandle;
    
    @Autowired
    @Qualifier("knowledgeServiceImpl")
    private KnowledgeService knowledgeService;
    
    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;
    
    
    //需要清理的用户数据
    private static final String NEED_CLEAN_USER_KEY = "_cache:key:user:data";
    //处理失败的用户数据
    private static final String WRONG_CLEAN_USER_KEY = "_wrong:cache:key:user:data";
    //所有的三级节点
    private static final Set<Long> THE_LAST_LEVEL_POINT = new HashSet<>();
    private static final int THE_LAST_LEVEL_POINT_NUM = 3;

    

    @Autowired
    public TestController(SpringContextPublisher springContextPublisher) {
        this.springContextPublisher = springContextPublisher;
    }

    @RequestMapping(value = "/test01", method = RequestMethod.GET)
    @AnalysisReport(value = EventType.HuaTuOnline_app_pc_HuaTuOnline_ShareTest)
    public Object test01() {
        EventEntity.newInstance(EventType.HuaTuOnline_app_pc_HuaTuOnline_ShareTest);
        EventEntity.putProperties(share_type, "app");
        EventEntity.putProperties(test_id, 123123);
        EventEntity.putProperties(test_first_cate, "常识判断");
        EventEntity.putProperties(test_second_cate, "人文");
        EventEntity.putProperties(test_third_cate, "建筑");
        EventEntity.putProperties(test_correct_rate, 0.8f);
        EventEntity.putProperties(test_answer_duration, 23819382893L);
        EventEntity.putProperties(test_type, "专项练习");
        EventEntity.getInstance().setDistinctId("lololo");
        springContextPublisher.pushEvent(new AnalysisEvent(EventEntity.getInstance()));
        Map<String, String> lol = new HashMap<>();
        lol.put("lol", "loo");
        return lol;
    }

    @RequestMapping("/test02")
    public Object test02() {
        Map<String, Object> properties = new HashMap<>(16);
        properties.put("search_message_type", "test");
        properties.put("search_keyword", "jbzm");
        EventEntity.getInstance().setDistinctId("lololo");
        return System.currentTimeMillis();
    }

    @RequestMapping("clear/{userId}")
    public Object clearCache(@PathVariable long userId){
        poxyUtilService.getQuestionErrorService().clearRedisCache(userId);
        poxyUtilService.getQuestionFinishService().clearRedisCache(userId);
        return SuccessMessage.create("操作成功");
    }
    
    
    @RequestMapping("sendInfo2Redis/{userId}")
    public Object sendInfo2Redis(@PathVariable Long userId) {
        com.google.common.base.Stopwatch stopwatch = com.google.common.base.Stopwatch.createStarted();


        //1.处理搜藏数据
        QuestionPersistenceEnum.TableName collectTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT;
        List<QuestionPersistenceModel> collectQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(),
                collectTable);
        collectQuestionPersistenceModels.forEach(model -> {
            String pointId = model.getQuestionPointId();
            String collectSetKey = RedisKnowledgeKeys.getCollectSetKey(userId, Integer.valueOf(pointId));

            List<String> collectCacheData = service.getCollectCacheData(collectSetKey);
            judgeIsOutOfExpiredTime(userId, collectCacheData, pointId, collectSetKey, collectTable, model);
        });
        log.info("stop" + String.valueOf(stopwatch.stop()));

        //2.处理错误数据
        QuestionPersistenceEnum.TableName wrongTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG;
        final List<QuestionPersistenceModel> wrongQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(),
                wrongTable);
        wrongQuestionPersistenceModels.forEach(model -> {
            String pointId = model.getQuestionPointId();
            String wrongKey = RedisKnowledgeKeys.getWrongSetKey(userId, Integer.valueOf(pointId));
            List<String> wrongCacheData = service.getWrongCacheData(wrongKey);
            judgeIsOutOfExpiredTime(userId, wrongCacheData, pointId, wrongKey, wrongTable, model);
        });
        log.info("错题刷新到redis,完毕");
		return "save 2 redis complate.";


    
        
    }
    
    @RequestMapping("clearAllRedisData/{userId}")
    public Object clearAllRedisData(@PathVariable Long userId) {


        final Long userIdLong = Long.valueOf(userId);
        long mark = System.currentTimeMillis();
        long markCollect = 0;
        try {
            log.info("开始数据清理");
            //开始清理收藏数据
            String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userIdLong);
            Set<String> collectSet = hashOperations.entries(collectCountKey).keySet();
            if (CollectionUtils.isNotEmpty(collectSet)) {
                Function<String, List<String>> collectFunction = (cacheKey) -> {
                    List<String> collectCacheData = service.getCollectCacheData(cacheKey);
                    return collectCacheData;
                };

                Set<String> collectPointKeySet = collectSet.stream()
                        .map(point -> RedisKnowledgeKeys.getCollectSetKey(userIdLong, Integer.valueOf(point)))
                        .collect(Collectors.toSet());
                doCleanPoint(collectPointKeySet, collectFunction,
                        QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_COLLECT,
                        QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT);
            }
            markCollect = System.currentTimeMillis() - mark;
            log.info("处理用户 {} 收藏数据,所用时长：{}，共计时长，{}", userId, markCollect, markCollect);
        } catch (Exception e) {
            log.info("用户 {} 收藏数据处理异常 >>>>>>>", userId);
        }

        //开始清理错题数据
        long markWrong = 0;
        try {
            String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userIdLong);
            Set<String> wrongSet = hashOperations.entries(wrongCountKey).keySet();
            if (CollectionUtils.isNotEmpty(wrongSet)) {
                Function<String, List<String>> wrongFunction = (cacheKey) -> {
                    List<String> wrongCacheData = service.getWrongCacheData(cacheKey);
                    return wrongCacheData;
                };
                Set<String> wrongPointKeySet = wrongSet.stream()
                        .map(point -> RedisKnowledgeKeys.getWrongSetKey(userIdLong, Integer.valueOf(point)))
                        .collect(Collectors.toSet());
                doCleanPoint(wrongPointKeySet, wrongFunction,
                        QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_WRONG,
                        QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG
                );
            }
            markWrong = System.currentTimeMillis() - mark;
            log.info("处理用户 {} 错题数据,所用时长：{}，共计时长，{}", userId, markWrong - markCollect, markWrong);
        } catch (Exception e) {
            log.info("用户 {} 错题数据处理异常 >>>>>>>", userId);
        }


        //开始清理已完成数据 - 已完成数据 存储在SSDB中且 只有三级节点有试题数据，需要过滤掉一、二级数据
        Consumer<SsdbConnection> ssdbConnectionConsumer = connection -> {
            try {
                Set<String> finishSet = connection.hgetall(RedisKnowledgeKeys.getFinishedCountKey(userIdLong)).keySet();
                if (CollectionUtils.isNotEmpty(finishSet)) {
                    Function<String, List<String>> finishFunction = (cacheKey) -> {
                        List<String> finishCacheData = service.getFinishCacheData(cacheKey);
                        return finishCacheData;
                    };
                    Set<String> finishKeySet = finishSet.stream()
                            //此处只需要处理三级节点的数据
                            .filter(point -> THE_LAST_LEVEL_POINT.contains(Long.valueOf(point)))
                            .map(point -> RedisKnowledgeKeys.getFinishedSetKey(userIdLong, Integer.valueOf(point)))
                            .collect(Collectors.toSet());
                    doCleanPoint(finishKeySet, finishFunction,
                            QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_FINISH,
                            QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_FINISH
                    );
                }
            } catch (Exception e) {
                log.info("用户 {} 已完成数据处理异常 >>>>>>>", userId);
            }
        };
        Consumer<Exception> exceptionConsumer = (exception) -> log.info("QuestionDataHandleState >>处理收藏信息异常 ,异常信息:{}", exception.getMessage());
        getSSDBConnectionAndDoSomething(ssdbConnectionConsumer, exceptionConsumer);
        
        log.info("开始重组redis中hash 知识点count数据 >>>>>>>", userId);

        String collectCountKey = RedisKnowledgeKeys.getWrongCountKey(userId);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        hashOperations.entries(collectCountKey)
                .entrySet().stream()
                .forEach(entry -> {
                    String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(userId, Integer.valueOf(entry.getKey()));
                    Long size = zSetOperations.size(wrongSetKey);
                    hashOperations.put(collectCountKey,entry.getKey(),size + "");
                });
    
        
        return "clearAllRedisData success";
    }
    
    /**
     * 处理redis中是否有此key(redis中是否有此key,如果没有,直接将mysql写入redis)
     * 更新时间以缓存时间90天为界限,只刷新90天之前的数据
     */
    public void judgeIsOutOfExpiredTime(Long userId, List<String> list, String pointId, String key, QuestionPersistenceEnum.TableName tableName,
                                        QuestionPersistenceModel model) {
        //更新时间是否在10号之前
        if (CollectionUtils.isEmpty(list)) {
            this.writeDataToRedis(key, userId, pointId, tableName, false);
        } else {
            if (null != model.getUpdateTime()) {
                Date startTime = model.getUpdateTime();
                Instant instant = startTime.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate startLocalDate = instant.atZone(zoneId).toLocalDate();
                long until = startLocalDate.until(LocalDate.now(), ChronoUnit.DAYS);
                if (until > 90) {
                    this.writeDataToRedis(key, userId, pointId, tableName, true);
                }
            }
        }
    }

    /**
     * @param key       reids key
     * @param userId    用户ID
     * @param pointId   知识点ID
     * @param tableName 表名
     * @param flag      是否刷新此key数据到redis
     */
    private void writeDataToRedis(String key, Long userId, String pointId, QuestionPersistenceEnum.TableName tableName, Boolean flag) {

        //补偿策略 - 回写数据
        List<String> questionList = service.getQuestionIdByUserIdAndPointId(userId.toString(), pointId, tableName);
        List<String> questionListResult = new ArrayList<>(questionList);
        if (null == questionList || questionList.size() == 0) {
            return;
        }
        if (flag) {
            List<String> questionsFromRedis = service.getCollectCacheData(key);
            questionListResult.addAll(questionsFromRedis);
        }

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        long time = System.currentTimeMillis() / 1000 - 1000;
        for (int index = 0; index < questionList.size(); index++) {
            zSetOperations.add(key, questionList.get(index), time - index * 1000);
        }
        redisTemplate.expire(key, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
    }

    public void doCleanPoint(
            Set<String> allKeySet,
            Function<String, List<String>> function,
            QuestionPersistenceEnum.RedisKey cacheRedisKey,
            QuestionPersistenceEnum.TableName tableName) {
        allKeySet.forEach(cacheKey -> {
            //构建 缓存数据对象 执行缓存操作
            HashMap<String, String> map = QuestionPersistenceKeyUtil.cacheKeyToMap(cacheKey);
            if (MapUtils.isNotEmpty(map)) {
                List<String> list = function.apply(cacheKey);
                String questionIds = list.parallelStream().collect(Collectors.joining(","));
                QuestionPersistenceModel model = new QuestionPersistenceModel(map.get("userId"), map.get("pointId"), questionIds);
                //数据处理
                dataHandle.questionDataHandle(model, cacheRedisKey, tableName);
            }
        });
    }

    /**
     * 获取所有的三级节点信息
     */
    private void initTheLastLevelPoint() {
        if (CollectionUtils.isEmpty(THE_LAST_LEVEL_POINT)) {
            Example example = Example.builder(Knowledge.class).build();
            example.and().andEqualTo("level", THE_LAST_LEVEL_POINT_NUM);
            List<Knowledge> knowledgeList = knowledgeService.selectByExample(example);
            THE_LAST_LEVEL_POINT.clear();
            THE_LAST_LEVEL_POINT.addAll(knowledgeList.stream().map(Knowledge::getId).collect(Collectors.toSet()));
        }
    }

    /**
     * 获取连接信息
     *
     * @param consumer          正常连接后的 消费行为
     * @param exceptionConsumer 异常后的 消费行为
     */
    private void getSSDBConnectionAndDoSomething(Consumer<SsdbConnection> consumer, Consumer<Exception> exceptionConsumer) {
        SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        try {
            consumer.accept(connection);
        } catch (Exception e) {
            exceptionConsumer.accept(e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
    }

}