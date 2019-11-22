package com.huatu.ztk.search.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author zhengyi
 * @date 2019-03-07 17:21
 **/
@Component
public class RedisCacheUpdate {

    @Autowired
    private RedisTemplate redisTemplate;

    public void updateValue(Object key, Object resource) {
        redisTemplate.opsForValue().set(key, resource);
    }
}