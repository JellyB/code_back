package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.service.VersionService;
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
 * Create time 2019-06-14 10:42 AM
 **/
@Slf4j
public class RedisTest extends BaseWebTest {


    @Autowired
    private VersionService versionService;

    @Autowired
    @Qualifier(value = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier(value = "persistTemplate")
    private RedisTemplate  persistTemplate;


    @Test
    public void iosAudit(){
        String cv = "7.1.41";
        Boolean audit = versionService.isIosAudit(2, cv);
        log.error("audit --------- {}", audit);
    }


    @Test
    public void addRedis(){
        String key = "redis.test.1111_00000000";
        String key_ = "redis.test.2222_0000000";
        String value = "world";
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
        redisTemplate.expire(key,100, TimeUnit.SECONDS);

        ValueOperations valueOperations_ = persistTemplate.opsForValue();
        valueOperations_.set(key_, value);
        persistTemplate.expire(key_,100, TimeUnit.SECONDS);

        log.error(">>>>>>>>>>>>>> get from redisTemplate:{}", valueOperations.get(key));
        log.error(">>>>>>>>>>>>>> get from persistTemplate:{}", valueOperations_.get(key));
    }
}
