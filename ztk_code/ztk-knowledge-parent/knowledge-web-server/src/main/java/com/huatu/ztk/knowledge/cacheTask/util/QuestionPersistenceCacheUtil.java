package com.huatu.ztk.knowledge.cacheTask.util;

import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by junli on 2018/3/21.
 */
@Component
public class QuestionPersistenceCacheUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private QuestionDataHandleState dataHandle;

    public void cacheWrongQuestions() {
        Function<String, List<String>> function = (cacheKey) -> {
            List<String> wrongCacheData = service.getWrongCacheData(cacheKey);
            return wrongCacheData;
        };
        cacheTaskCommon(
                QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_WRONG,
                QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG,
                function
        );
    }

    public void cacheCollectQuestions() {
        Function<String, List<String>> function = (cacheKey) -> {
            List<String> collectCacheData = service.getCollectCacheData(cacheKey);
            return collectCacheData;
        };
        cacheTaskCommon(
                QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_COLLECT,
                QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT,
                function
        );

    }

    public void cacheFinishQuestions() {
        Function<String, List<String>> function = (cacheKey) -> {
            List<String> finishCacheData = service.getFinishCacheData(cacheKey);
            return finishCacheData;
        };
        cacheTaskCommon(
                QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_FINISH,
                QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_FINISH,
                function
        );

    }

    /**
     * 存储 有变更的用户试题数据
     *
     * @param cacheRedisKey
     * @param tableName     存储的表名称
     * @param function      redis 中的key 转换成 考题信息 实现
     */
    protected void cacheTaskCommon(QuestionPersistenceEnum.RedisKey cacheRedisKey, QuestionPersistenceEnum.TableName tableName, Function<String, List<String>> function) {
        //1.根据Redis 中缓存的key 值 获取所有需要修改的key 值
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        /**
         * 此处单独处理每个redis 的key 值
         * 解决多个服务器之间的服务资源竞争问题
         */
        while (true) {
            final String cacheKey = setOperations.pop(cacheRedisKey.getRedisKey());
            if (StringUtils.isBlank(cacheKey)) {
                break;
            }
            //2.构建 缓存数据对象 执行缓存操作
            HashMap<String, String> map = QuestionPersistenceKeyUtil.cacheKeyToMap(cacheKey);
            if (MapUtils.isNotEmpty(map)) {
                List<String> list = function.apply(cacheKey);
                String questionIds = list.parallelStream().collect(Collectors.joining(","));
                QuestionPersistenceModel model = new QuestionPersistenceModel(map.get("userId"), map.get("pointId"), questionIds);
                //TODO : 切换成 缓存清理的逻辑
                //dataHandle.questionDataHandle(model, cacheRedisKey, tableName);
                service.save(model, tableName);
            }
        }
    }

}
