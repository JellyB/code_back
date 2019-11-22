package com.huatu.ztk.monitor.task;

import com.google.common.cache.Cache;
import com.huatu.ztk.monitor.common.MonitorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

/**
 * Created by linkang on 1/10/17.
 */
public class LogReadTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(LogReadTask.class);

    @Override
    public void onMessage(Message message) {
        //knowledge-web-server.192.168.100.24
        final String appId = message.getMessageProperties().getAppId();

        final Map<String, Object> headers = message.getMessageProperties().getHeaders();
        //日志级别
        final String level = (String) headers.get("level");
        String serverName = appId.substring(0,appId.indexOf("."));


        String ip = appId.substring(appId.indexOf(".") + 1);

        if (ip.equals("192.168.100.22")) {
            return;
        }

        if (level.equals("ERROR")) {
            // TODO: 1/10/17 guava cache
            /**user122189 3
             * user122190 2
             * 一分钟为单位,servername+分钟作为key value:报警次数,key保存6小时的
             * 启动一个定时任务,去遍历,达到条件报警
             */
            final long errorTime = LocalDateTime.now()
                    .truncatedTo(ChronoUnit.MINUTES)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            String key = serverName + "_" + errorTime;
            Cache<String, Integer> cache = MonitorCache.countCache;
            int errorCount = Optional.ofNullable(cache.getIfPresent(key)).orElse(0);
            cache.put(key, errorCount + 1);
            logger.info("put key={},count={}", key, errorCount + 1);
        }
    }


}
