package com.huatu.ztk.knowledge.dataClean;

import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 用户行为数据清除
 * Created by lijun on 2018/9/10
 */
public class QuestionRecordClean extends BaseTest {

    @Autowired
    private RedisTemplate redisTemplate;

    //用户ID
    private static final Integer USER_ID = 233906500;

    @Test
    public void cleanWrongQuestion() {
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(USER_ID);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.entries(wrongCountKey)
                .entrySet().stream()
                .forEach(entry -> {
                    System.out.println(RedisKnowledgeKeys.getWrongSetKey(USER_ID, Integer.valueOf(entry.getKey())));
                    redisTemplate.delete(RedisKnowledgeKeys.getWrongSetKey(USER_ID, Integer.valueOf(entry.getKey())));
                });
        redisTemplate.delete(wrongCountKey);
    }

    @Test
    public void cleanCollectQuestion() {
        String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(USER_ID);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.entries(collectCountKey)
                .entrySet().stream()
                .forEach(entry -> redisTemplate.delete(RedisKnowledgeKeys.getCollectSetKey(USER_ID, Integer.valueOf(entry.getKey()))));
        redisTemplate.delete(collectCountKey);
    }
}
