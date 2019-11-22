package com.huatu.ztk.backend.task;

import com.huatu.ztk.backend.constant.RedisKeyConstant;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 散题定时刷新和定时下载
 */
@Component
@Slf4j
public class QuestionDownLoadTask {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDownLoadTask.class);
    @Autowired
    RedisTemplate redisTemplate;
    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }
//    @Scheduled(fixedRate = 60000)
    public void questionDownload() throws BizException {
        if (!getLock()) {
            return;
        }
        try {



        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            unlock();
        }
    }


    private String getServerIp() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
        String hostAddress = address.getHostAddress();//192.168.0.121
        return hostAddress;
    }

    /**
     * 释放定时任务锁
     */
    private void unlock() {
        String lockKey = RedisKeyConstant.getQuestionDownloadLock();
        String currentServer = (String)redisTemplate.opsForValue().get(lockKey);

        logger.info("current server={}",currentServer);
        String serverIp = "";
        try {
            serverIp = getServerIp();
            logger.info("getServerIp:"+getServerIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (serverIp.equals(currentServer)) {
            redisTemplate.delete(lockKey);

            logger.info("release lock,server={},timestamp={}",currentServer,System.currentTimeMillis());
        }
    }

    /**
     *
     * @return 是否获得锁
     */
    private boolean getLock() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = RedisKeyConstant.getQuestionDownloadLock();
        String value = opsForValue.get(lockKey);

        logger.info("get lock timestamp={},value={}",System.currentTimeMillis(),value);
        String serverIp = "";
        try {
            serverIp = getServerIp();
            logger.info("getServerIp:"+getServerIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, serverIp).booleanValue();
            log.info("booleanValue：{}，当前定时器被{}锁定",booleanValue,opsForValue.get(lockKey));

            if(booleanValue || serverIp.equals(opsForValue.get(lockKey))){
                return true;
            }else{
                return false;
            }

        } else if (StringUtils.isNoneBlank(value) && !value.equals(serverIp)) {
            //被其它服务器锁定
            logger.info("auto submit match lock server={},return", value);
            return false;
        } else { //被自己锁定
            return true;
        }
    }
}
