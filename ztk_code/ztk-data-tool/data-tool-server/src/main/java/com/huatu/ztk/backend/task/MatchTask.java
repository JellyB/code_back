package com.huatu.ztk.backend.task;

import com.huatu.ztk.backend.constant.RedisKeyConstant;
import com.huatu.ztk.backend.metas.controller.PracticeMetaController;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.PaperStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 散题定时刷新和定时下载
 */
@Component
@Slf4j
@EnableScheduling
public class MatchTask {
    private static final Logger logger = LoggerFactory.getLogger(MatchTask.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    MatchDao matchDao;
    @Autowired
    PracticeMetaController practiceMetaController;
    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }
    //每天每小时30-35分，每分钟刷新一次
//    @Scheduled(cron = "0 30-35 * * * ?")
    public void matchEnroll() throws BizException {
        log.info("定时开始++++++++++");
        if (!getLock()) {
            return;
        }
        log.info("定时正在进行++{}",new Date());
        try {
            List<Match> matches = matchDao.findAll().stream().filter(i -> i.getStatus() == PaperStatus.AUDIT_SUCCESS).collect(Collectors.toList());
            filterTodayMatch(matches);
            if(CollectionUtils.isEmpty(matches)){
                log.info("没有符合导出报名数据的模考大赛试卷");
                return;
            }
            long nowTime = System.currentTimeMillis();
            for (Match match : matches) {
                if(match.getStartTime()+ TimeUnit.MINUTES.toMillis(30) <= nowTime && nowTime <match.getStartTime() + TimeUnit.MINUTES.toMillis(40)){
                    logger.info("模考大赛报名数据导出："+match.getName());
                    practiceMetaController.getMatchEnrollInfo(match.getPaperId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            unlock();
        }
    }

    //每天每小时5-10分钟，每分钟一次
    @Scheduled(cron = "0 5-10 * * * ?")
    public void matchCount() throws BizException {
        if (!getLock()) {
            return;
        }
        try {
            List<Match> matches = matchDao.findAll().stream().filter(i -> i.getStatus() == PaperStatus.AUDIT_SUCCESS).collect(Collectors.toList());
            filterTodayMatch(matches);
            if(CollectionUtils.isEmpty(matches)){
                log.info("没有符合导出考试数据的模考大赛试卷");
                return;
            }
            long nowTime = System.currentTimeMillis();
            for (Match match : matches) {
                if(match.getEndTime() <= nowTime && nowTime <match.getEndTime() + TimeUnit.MINUTES.toMillis(15)){
                    logger.info("模考大赛考试数据准备导出："+match.getName());
                    practiceMetaController.getMatchMetaInfo(match.getPaperId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            unlock();
        }
    }

    /**
     * 筛选当天的考试
     * @param matches
     */
    private void filterTodayMatch(List<Match> matches) {
        Date nowDate = new Date();
        matches.removeIf(i-> !DateUtils.isSameDay(nowDate,new Date(i.getStartTime())));
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
        String lockKey = RedisKeyConstant.getMatchCountLock();
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

        String lockKey = RedisKeyConstant.getMatchCountLock();
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
