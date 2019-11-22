package com.huatu.tiku.teacher.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class TaskService {

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }

    public void task(){
        try{
            if (!getLock()) {
                return;
            }
            run();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            unlock();
        }
    }

    /**
     * 执行
     */
    public abstract void run();
    private boolean getLock() {
        System.out.println("check0");
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = getCacheKey();

        String value = opsForValue.get(lockKey);

        log.info("get lock timestamp={}",System.currentTimeMillis());
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) { //值为空
            System.out.println("check1");
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, getLocalTimeLock()).booleanValue();
            if(booleanValue){
                System.out.println("check1.1");
                redisTemplate.expire(lockKey,getExpireTime(), TimeUnit.MINUTES);
            }
            return booleanValue;
        } else if (org.apache.commons.lang3.StringUtils.isNoneBlank(value) && !value.equals(getLocalTimeLock())) {
            System.out.println("check2");
            //被其它服务器锁定
            return false;
        } else { //被自己锁定
            System.out.println("check3");
            return true;
        }
    }

    protected abstract long getExpireTime();


    public void unlock() {
        String lockKey = getCacheKey();
        String currentServer = (String) redisTemplate.opsForValue().get(lockKey);

        log.info("current server={}",currentServer);
        if (getLocalTimeLock().equals(currentServer)) {
            redisTemplate.delete(lockKey);
            log.info("release lock,server={},timestamp={}",currentServer,System.currentTimeMillis());
        }
        setNanoTime("");
    }

    public abstract String getCacheKey();
    public abstract String getNanoTime();
    public abstract void setNanoTime(String nanoTime);




    private String getLocalTimeLock() {
        if(StringUtils.isBlank(getNanoTime())){
            setNanoTime(System.nanoTime()+"");
        }
        return getNanoTime();
    }
}
