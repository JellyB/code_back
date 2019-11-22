package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hanchao
 * @date 2017/10/11 17:00
 */
@Component
@Slf4j
public class ThirdNotifyListener {
    private static final String REWARD = "reward";
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "third-plat-notify")
    public void onMessage(Message message){
        try {
            String body = new String(message.getBody());
            System.out.println(body);
            JSONObject object = JSON.parseObject(body);
            if(REWARD.equalsIgnoreCase(object.getString("type"))){
                //任务消息
                RewardMessage rewardMessage =objectMapper.readValue(String.valueOf(object.get("data")),RewardMessage.class);
                eventPublisher.publishEvent(RewardActionEvent.class,
                        this,
                        (event) -> event.setAction(RewardAction.ActionType.valueOf(rewardMessage.getAction()))
                                .setUname(rewardMessage.getUname())
                                .setUid(rewardMessage.getUid())
                                .setBizId(rewardMessage.getBizId())
                                .setGold(rewardMessage.getGold())
                                .setExperience(rewardMessage.getExperience())
                );
            }
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }

}
