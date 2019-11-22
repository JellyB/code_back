package com.huatu.ztk.knowledge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class RedisDebugService {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final boolean testFlag = true;

    public  Consumer<String> test = (key->{
        if(testFlag){
//            System.out.println("test = "+key);
            Long expire = redisTemplate.getExpire(key);
            if(expire > TimeUnit.MINUTES.toSeconds(1) && expire < TimeUnit.DAYS.toSeconds(7)){
                redisTemplate.expire(key,1,TimeUnit.MINUTES);
            }
            if(expire.intValue() == -1){
                redisTemplate.delete(key);
            }
        }
    });


}
