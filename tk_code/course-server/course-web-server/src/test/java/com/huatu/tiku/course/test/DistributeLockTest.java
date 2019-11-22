package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.util.RedisLockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-06 3:54 PM
 **/

@Slf4j
public class DistributeLockTest extends BaseWebTest {

    @Autowired
    private RedisLockHelper redisLockHelper;

    @Test
    public void test(){

        int timeOut = 5 * 1000;
        Random random = new Random();
        for(int i = 0; i < 10; i ++){
            String targetId = String.valueOf(random.nextInt(1000));
            log.info("=========================:{}", targetId);
            String time = System.currentTimeMillis() + timeOut + "";
            new Thread(() -> {
                if(redisLockHelper.lock(targetId, time, 3, TimeUnit.MINUTES)){
                    System.err.println("locked thread 1 " + targetId);
                }}).start();
            new Thread(() -> {
                if(redisLockHelper.lock(targetId, time,3, TimeUnit.MINUTES)){
                    System.err.println("locked thread 2 " + targetId);
                }}).start();
            new Thread(() -> {
                if(redisLockHelper.lock(targetId, time,3, TimeUnit.MINUTES)){
                    System.err.println("locked thread 3 " + targetId);
                }}).start();
            new Thread(() -> {
                if(redisLockHelper.lock(targetId, time,3, TimeUnit.MINUTES)){
                    System.err.println("locked thread 4 " + targetId);
                }}).start();
        }
    }
}
