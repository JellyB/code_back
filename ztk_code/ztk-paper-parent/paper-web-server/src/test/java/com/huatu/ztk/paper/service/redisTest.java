package com.huatu.ztk.paper.service;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Set;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/17
 * @描述
 */
public class redisTest extends BaseTest {

    @Autowired
    RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(redisTest.class);

    @Test
    public void testRedis() {

    }
}
