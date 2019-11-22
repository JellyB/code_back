package com.huatu.ztk.monitor.task;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.monitor.common.MonitorCache;
import com.huatu.ztk.monitor.common.MonitorConstants;
import com.huatu.ztk.sms.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.cache.CacheBuilder.newBuilder;

/**
 * Created by linkang on 1/10/17.
 */

@Component
public class WarnTask {
    private static final Logger logger = LoggerFactory.getLogger(WarnTask.class);

    //用作发短信的标记
    private static final Cache<String, WarnMark> markCache =
            newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .build();


    //1分钟执行一次
    @Scheduled(cron = "0 0/1 * * * ?")
    public void sendWarnMsg() throws BizException {
        Cache<String, Integer> cache = MonitorCache.countCache;
        Map<String,Integer> serverErrorMap = Maps.newHashMap();
        final long minTime = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES)
                .minusMinutes(MonitorConstants.MAX_PAST_MIN)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        for (String key : cache.asMap().keySet()) {
            final String[] values = key.split("_");
            final Long errorTime = Long.valueOf(values[1]);
            String serverName = values[0];
            if (errorTime > minTime) {//在报警的错误范围内
                //累加服务的报警次数
                serverErrorMap.put(serverName,serverErrorMap.getOrDefault(serverName,0)+cache.getIfPresent(key));
            }
        }

        for (Map.Entry<String, Integer> entry : serverErrorMap.entrySet()) {
            //达到报错条件
            final Integer errorCount = entry.getValue();
            final String serverName = entry.getKey();
            if (errorCount < MonitorConstants.MAX_ERROR_COUNT) {
                continue;
            }
            if (!isSend(serverName,errorCount)) {
                continue;
            }
            sendSms(serverName, errorCount);
            WarnMark warnMark = markCache.getIfPresent(serverName);

            if (warnMark == null) {
                warnMark = new WarnMark();
            }

            final long currentTime = LocalDateTime.now()
                    .truncatedTo(ChronoUnit.MINUTES)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            warnMark.setWarnCount(warnMark.getWarnCount()+1);//报警次数累计+1
            warnMark.setWarnTime(currentTime);
            warnMark.setErrorCount(errorCount);  //设置最新的错误计数
            markCache.put(serverName,warnMark);
        }
    }

    /**
     * 判断是否需要发送报警短信
     *
     * @param serverName
     * @return
     */
    private boolean isSend(String serverName, int errorCount) {
        WarnMark warnMark = markCache.getIfPresent(serverName);
        if (warnMark == null) {//不存在，说明之前还没有进行过报警
            return true;
        }
        final long currentTime = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        //判断当前时间是否达到了报警的时候，与上一次错误计数比较，如果增加就发送
        if (currentTime > warnMark.getWarnTime() + TimeUnit.MINUTES.toMillis(Math.min(warnMark.getWarnCount() * 3, 15))
                && errorCount > warnMark.getErrorCount()) {
            return true;
        }

        return false;
    }

    public static void sendSms(String serverName, int errorCount) {
        logger.info("send sms,phones={},serverName={},errorCount={}",
                MonitorConstants.PHONE_LIST, serverName, errorCount);
        SmsUtil.sendWarn(MonitorConstants.PHONE_LIST, serverName, errorCount);
    }

    class WarnMark{
        private long warnTime;   //发送时间
        private int warnCount;   //发送警告次数
        private int errorCount;  //错误计数

        public long getWarnTime() {
            return warnTime;
        }

        public void setWarnTime(long warnTime) {
            this.warnTime = warnTime;
        }

        public int getWarnCount() {
            return warnCount;
        }

        public void setWarnCount(int warnCount) {
            this.warnCount = warnCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }
    }

}
