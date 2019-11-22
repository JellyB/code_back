package com.huatu.tiku.course.util;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-06 1:53 PM
 **/

@Component
@Slf4j
public class RedisLockHelper {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁
     * @param targetId  业务 id
     * @param timeStamp 当前时间 + 超时时间
     * @return
     */
    public boolean lock(String targetId, String timeStamp, long timeout, TimeUnit unit){
        if(stringRedisTemplate.opsForValue().setIfAbsent(targetId, timeStamp)){
            log.info("set key expire time.1:{}", targetId);
            stringRedisTemplate.opsForValue().getOperations().expire(targetId, timeout, unit);
            return true; // key, 不存在，可以成功设置，上锁成功;
        }
        // 判断锁超时 - 防止原来的操作异常，没有运行解锁操作，防止死锁
        String currentLock = stringRedisTemplate.opsForValue().get(targetId);
        // 如果锁过期 currentLock 不为空且小于当前时间
        if(!Strings.isNullOrEmpty(currentLock) && Long.parseLong(currentLock) < System.currentTimeMillis()){
            // 获取上一个锁的时间 value，对应 get set，如果 lock 存在
            String preLock = stringRedisTemplate.opsForValue().getAndSet(targetId, timeStamp);
            if(!Strings.isNullOrEmpty(preLock) && preLock.equals(currentLock)){
                log.info("set key expire time.2:{}", targetId);
                stringRedisTemplate.opsForValue().getOperations().expire(targetId, timeout, unit);
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param target
     * @param timeStamp
     */
    public void unlock(String target, String timeStamp){
        try{
            String currentValue = stringRedisTemplate.opsForValue().get(target);
            if(!Strings.isNullOrEmpty(currentValue) && currentValue.equals(timeStamp)){
                //删除锁
                stringRedisTemplate.opsForValue().getOperations().delete(target);
            }
        }catch (Exception e){
            log.error("警报！警报！警报！警报！:{}", e);
        }
    }
}
