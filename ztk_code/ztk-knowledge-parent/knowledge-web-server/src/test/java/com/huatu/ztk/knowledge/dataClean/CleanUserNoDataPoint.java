package com.huatu.ztk.knowledge.dataClean;

import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.junit.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * Created by lijun on 2018/9/26
 */
public class CleanUserNoDataPoint extends BaseTest {

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    //用户ID
    final long USER_ID = 233149314;

    @Test
    public void test(){
        System.out.println("  数据清洗开始 ");
        clean(RedisKnowledgeKeys::getCollectSetKey,RedisKnowledgeKeys::getCollectCountKey);
        System.out.println(" 收藏数据清理完成 ");

        clean(RedisKnowledgeKeys::getWrongSetKey,RedisKnowledgeKeys::getWrongCountKey);
        System.out.println(" 错题数据清理完成 ");
    }

    private void clean(BiFunction<Long,Integer,String> setKey, Function<Long,String> hashKey){
        String hashKeyStr = hashKey.apply(USER_ID);
        hashOperations.entries(hashKeyStr)
                .keySet().stream()
                .forEach(key -> {
                    String setKeyStr = setKey.apply( USER_ID,Integer.valueOf(key));
                    Long size = zSetOperations.zCard(setKeyStr);

                    System.out.println(" key = " + setKeyStr + ", size = " + size + ",hashSize = " + hashOperations.get(hashKeyStr,key));
                    if (null == size || size == 0){
                        hashOperations.delete(hashKeyStr,key);
                    }
                });
    }
}
