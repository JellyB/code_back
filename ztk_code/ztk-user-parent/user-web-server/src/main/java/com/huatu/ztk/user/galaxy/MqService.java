package com.huatu.ztk.user.galaxy;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author jbzm
 * @date Create on 2018/3/19 16:42
 */
@Async
@Component
public class MqService {
    private static final Logger logger = Logger.getLogger(MqService.class);
    /**
     * Token容器
     */
    private Set<String> tokens = new HashSet<>();
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
     *
     * @param token
     */
    public void send(String token) {
        try {
            if (token == null) {
                return;
            } else {
                tokens.add(token);
            }
            if (tokens.size() > CACHE_LENGTH) {
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
            if (tokens.size() > 0) {
                sendToMq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToMq() {
        if (tokens.size() > 0) {
            Set<String> tempTokens = new HashSet<>(tokens);
            tokens.clear();
            rabbitTemplate.convertAndSend("galaxy_data_topic", "com.galaxy.active", tempTokens);
            logger.info("数据上报:" + tempTokens.size());
        }
    }
}
