package com.huatu.ztk.knowledge.dataClean;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionDataHandleState;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/9/25
 */
public class CleanAllDataService extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CleanAllDataService.class);


    private static ThreadPoolExecutor threadPoolExecutor;
    private static SynchronousQueue<Runnable> workQueue = new SynchronousQueue(false);

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;
    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Autowired
    private QuestionDataHandleState dataHandle;

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    @Qualifier("knowledgeServiceImpl")
    private KnowledgeService knowledgeService;

    //需要清理的用户数据
    private static final String NEED_CLEAN_USER_KEY = "_cache:key:user:data";
    //处理失败的用户数据
    private static final String WRONG_CLEAN_USER_KEY = "_wrong:cache:key:user:data";
    //所有的三级节点
    private static final Set<Long> THE_LAST_LEVEL_POINT = new HashSet<>();
    private static final int THE_LAST_LEVEL_POINT_NUM = 3;

    //需要清理的用户ID
    private static final long userId = 233107392L;

    @Test
    public void test(){

        final Long userIdLong = Long.valueOf(userId);
        long mark = System.currentTimeMillis();
        long markCollect = 0;
        try {
            logger.info("开始数据清理");
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
