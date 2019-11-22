package com.huatu.ztk.knowledge.dataClean;

import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * Created by lijun on 2018/9/4
 */
public class WrongDataNumClean extends BaseTest {

    @Autowired
    private RedisTemplate redisTemplate;

    //用户ID
    private static final Integer USER_ID = 234298599;

    @Test
    public void test() {
        String collectCountKey = RedisKnowledgeKeys.getWrongCountKey(USER_ID);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        hashOperations.entries(collectCountKey)
                .entrySet().stream()
                .forEach(entry -> {
                    String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(USER_ID, Integer.valueOf(entry.getKey()));
                    Long size = zSetOperations.size(wrongSetKey);
                    hashOperations.put(collectCountKey,entry.getKey(),size + "");
                });
    }

}
