package com.huatu.tiku.match.initTest;

import com.huatu.common.test.BaseWebTest;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.List;

/**
 * Redis 连接测试
 * Created by lijun on 2018/10/11
 */
public class RedisBaseTest extends BaseWebTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired @Qualifier("redisTemplateWithoutServerName")
    private RedisTemplate redisTemplateWithoutServerName;

    @Resource(name = "redisTemplate")
    private ValueOperations<String,String> value;

    @Test
    public void testSet() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        valueOperations.set(testKey(), "testValue");
    }

    @Test
    public void testGet() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        System.out.println(valueOperations.get(testKey()));
    }

    @Test
    public void testResourceGet(){
        System.out.println(value.get(testKey()));
    }

    private static String testKey() {
        return "_test:key";
    }
}
