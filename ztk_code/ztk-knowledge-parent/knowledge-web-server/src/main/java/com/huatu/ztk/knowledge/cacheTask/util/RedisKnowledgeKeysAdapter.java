package com.huatu.ztk.knowledge.cacheTask.util;

import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by lijun on 2018/8/24
 */
@Component
public class RedisKnowledgeKeysAdapter {

    /**
     * 是否开启数据补偿
     */
    private final static boolean GET_CACHE_DATA_FROM_MYSQL = false;

    @Autowired
    private QuestionPersistenceDataGetUtil questionPersistenceDataGetUtil;

    @Resource
    private RedisTemplate redisTemplate;

    public final String getWrongSetKey(long uid, int point) {
        String errorSetKey = RedisKnowledgeKeys.getWrongSetKey(uid, point);
        /**
         * 数据补偿
         */
        if (GET_CACHE_DATA_FROM_MYSQL) {
            final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            questionPersistenceDataGetUtil.getWrongQuestionPersistence(errorSetKey, () ->
                    zSetOperations.range(errorSetKey, 0, -1)
            );
        }
        return errorSetKey;
    }

    /**
     * 收藏试题列表
     *
     * @param uid   用户id
     * @param point 知识点
     * @return
     */
    public final String getCollectSetKey(long uid, int point) {
        String collectSetKey = RedisKnowledgeKeys.getCollectSetKey(uid, point);
        /**
         * 数据补偿
         */
        if (GET_CACHE_DATA_FROM_MYSQL) {
            final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            questionPersistenceDataGetUtil.getCollectQuestionPersistence(collectSetKey, () ->
                    zSetOperations.range(collectSetKey, 0, -1)
            );
        }
        return collectSetKey;
    }

    @PostConstruct
    public void init() {
        InnerInstance.INSTANCE = this;
        InnerInstance.INSTANCE.questionPersistenceDataGetUtil = this.questionPersistenceDataGetUtil;
        InnerInstance.INSTANCE.redisTemplate = this.redisTemplate;
    }

    public static final RedisKnowledgeKeysAdapter getInstance() {
        return InnerInstance.INSTANCE;
    }

    private static class InnerInstance {
        public static RedisKnowledgeKeysAdapter INSTANCE = new RedisKnowledgeKeysAdapter();
    }
}
