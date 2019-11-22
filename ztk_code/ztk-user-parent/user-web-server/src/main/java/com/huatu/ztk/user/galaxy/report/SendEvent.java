package com.huatu.ztk.user.galaxy.report;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;


/**
 * @author jbzm
 * @date Create on 2018/3/19 16:42
 */
@Async
@Component
public class SendEvent {
    private static final Logger logger = Logger.getLogger(SendEvent.class);
    /**
     * Token容器
     */
    private List<Event> eventEntity = new LinkedList<>();
    /**
     * 最大时间间隔:ms
     */
    private static final long INTERVAL = 60000L;
    /**
     * 缓存长度  千万不要乱改
     */
    private static final int CACHE_LENGTH = 200;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 按量发送
     */
    public void send(Event event) {
        try {
            eventEntity.add(event);
            logger.info("event message:" + event.toString());
            if (eventEntity.size() >= CACHE_LENGTH) {
                sendToMq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时发送
     */
    @Scheduled(fixedRate = INTERVAL)
    public void timeSend() {
        try {
            if (eventEntity.size() > 0) {
                sendToMq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToMq() {
        if (eventEntity.size() > 0) {
            List<Event> tempTokens = new LinkedList<>(eventEntity);
            Object json = JSON.toJSON(tempTokens);
            eventEntity.clear();
            rabbitTemplate.convertAndSend("galaxy_data_topic", "com.galaxy.report", json);
            logger.info("数据上报->com.galaxy.report:" + tempTokens.size());
        }
    }
}
