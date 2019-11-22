package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-12 8:07 PM
 **/

@Slf4j
public class SentinelTest extends BaseWebTest {

    @Autowired
    @Qualifier(value = "redisTemplate")
    private RedisTemplate sentinelRedisTemplate;

    @Test
    public void put(){
        String key = "course.sentinel.key1234";
        String value = "helloWorld";
        int expire = 10;
        ValueOperations<String,String> valueOperations = sentinelRedisTemplate.opsForValue();
        valueOperations.set(key, value);
        sentinelRedisTemplate.expire(key, expire , TimeUnit.SECONDS);
    }
}
