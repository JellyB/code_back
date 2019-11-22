package com.huatu.tiku.interview.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.cache.RedisKeyConstant;
import com.huatu.tiku.interview.service.LearningReportService;
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

/**
 * Created by x6 on 2018/1/18.
 */
@Component
@Slf4j
public class SaveReportTask {

    @Autowired
    private LearningReportService learningReportService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    RedisTemplate redisTemplate;


    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }

    //每天晚十点（22：00）执行定时任务
    @Scheduled(cron="0 30 22 * * ?")
//    @Scheduled(fixedRate = 60000*5)
    public void submitMatchAnswer() throws BizException {
        if (!getLock()) {
            return;
        }
        String serverIp = "";
        try {
            serverIp = getServerIp();
            log.info("getServerIp:"+getServerIp());
            //处理业务
            //生成学习报告并推送
            learningReportService.dailyReport();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }finally {
            unlock();
        }
        log.info("auto save report task end.server={}", serverIp);
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
        String lockKey = RedisKeyConstant.SAVE_REPORT_LOCK;
        String currentServer = (String)redisTemplate.opsForValue().get(lockKey);

        log.info("current server={}",currentServer);
        String serverIp = "";
        try {
            serverIp = getServerIp();
            log.info("getServerIp:"+getServerIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (serverIp.equals(currentServer)) {
            redisTemplate.delete(lockKey);

            log.info("release lock,server={},timestamp={}",currentServer,System.currentTimeMillis());
        }
    }

    /**
     *
     * @return 是否获得锁
     */
    private boolean getLock() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = RedisKeyConstant.SAVE_REPORT_LOCK;
        String value = opsForValue.get(lockKey);
        log.info("get lock timestamp={}",System.currentTimeMillis());
        String serverIp = "";
        try {
            serverIp = getServerIp();
            log.info("getServerIp:"+getServerIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, serverIp).booleanValue();

            if(booleanValue || serverIp.equals(opsForValue.get(lockKey))){
                return true;
            }else{
                return false;
            }

        } else if (StringUtils.isNoneBlank(value) && !value.equals(serverIp)) {
            //被其它服务器锁定
            log.info("auto submit match lock server={},return", value);
            return false;
        } else { //被自己锁定
            return true;
        }
    }
}
