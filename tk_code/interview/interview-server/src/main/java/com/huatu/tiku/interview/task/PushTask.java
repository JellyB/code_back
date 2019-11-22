package com.huatu.tiku.interview.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.cache.RedisKeyConstant;
import com.huatu.tiku.interview.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created by x6 on 2018/1/18.
 */
@Component
@Slf4j
public class PushTask {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    private static String SERVER_IP = "";

    static {
        try {
            //获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
            InetAddress address = InetAddress.getLocalHost();
            SERVER_IP = address.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock(SERVER_IP)));
    }

    //每一分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void submitMatchAnswer() throws BizException {
        log.info("定时任务开始{}", SERVER_IP);
        if (!getLock(SERVER_IP)) {
            return;
        }
        try {
            notificationService.pushAuto();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unlock(SERVER_IP);
        }
        log.info("auto save report task end.server={}", SERVER_IP);
    }


    /**
     * 释放定时任务锁
     */
    private void unlock(String serverIp) {
        String lockKey = RedisKeyConstant.PUSH_NOTIFICATION_LOCK;
        String currentServer = (String) redisTemplate.opsForValue().get(lockKey);

        log.info("current server={}", currentServer);
        if (serverIp.equals(currentServer)) {
            redisTemplate.delete(lockKey);

            log.info("release lock,server={},timestamp={}", currentServer, System.currentTimeMillis());
        }
    }

    /**
     * @return 是否获得锁
     */
    private boolean getLock(String serverIp) {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = RedisKeyConstant.PUSH_NOTIFICATION_LOCK;
        String value = opsForValue.get(lockKey);
        log.info("get lock timestamp={}", System.currentTimeMillis());
        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, serverIp).booleanValue();
            if (booleanValue || serverIp.equals(opsForValue.get(lockKey))) {
                return true;
            } else {
                return false;
            }
        } else if (!serverIp.equals(value)) {
            //被其它服务器锁定
            log.info("auto submit match lock server={},return", value);
            return false;
        } else { //被自己锁定
            return true;
        }
    }
}
