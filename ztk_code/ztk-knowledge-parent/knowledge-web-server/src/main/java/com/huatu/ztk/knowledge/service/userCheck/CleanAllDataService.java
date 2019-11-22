package com.huatu.ztk.knowledge.service.userCheck;

import com.huatu.tiku.entity.knowledge.Knowledge;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/9/25
 */
@Service
public class CleanAllDataService {

    private static final Logger log = LoggerFactory.getLogger(CleanAllDataService.class);

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


    //所有的三级节点
    private static final Set<Long> THE_LAST_LEVEL_POINT = new HashSet<>();
    private static final int THE_LAST_LEVEL_POINT_NUM = 3;

    //清理用户错题数据
    public void cleanError(Long userId){
        long mark = System.currentTimeMillis();
        //开始清理错题数据
        long markWrong = 0;
        try {
            String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId);
            Set<String> wrongSet = hashOperations.entries(wrongCountKey).keySet();
            if (CollectionUtils.isNotEmpty(wrongSet)) {
                Function<String, List<String>> wrongFunction = (cacheKey) -> {
                    List<String> wrongCacheData = service.getWrongCacheData(cacheKey);
                    return wrongCacheData;
                };
                Set<String> wrongPointKeySet = wrongSet.stream()
                        .map(point -> RedisKnowledgeKeys.getWrongSetKey(userId, Integer.valueOf(point)))
                        .collect(Collectors.toSet());
                doCleanPoint(wrongPointKeySet, wrongFunction,
                        QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_WRONG,
                        QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG
                );
            }
            markWrong = System.currentTimeMillis() - mark;
            log.info("处理用户 {} 错题数据,所用时长：{}", userId,  markWrong);
        } catch (Exception e) {
            log.info("用户 {} 错题数据处理异常 >>>>>>>", userId);
            e.printStackTrace();
        }
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
