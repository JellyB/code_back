package com.huatu.tiku.course.mq.listeners;

import com.huatu.common.utils.encrypt.SignUtil;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.v3.UserServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/11 17:00
 */
@Component
@Slf4j
public class UserNickUpdateListener {

    @Autowired
    private UserServiceV3 userServiceV3;
    @Autowired
    private MessageConverter messageConverter;

    @RabbitListener(queues = RabbitConsts.QUEUE_USER_NICK_UPDATE)
    public void onMessage(Message message){
        try {
            Map<String,Object> map = (Map<String, Object>) messageConverter.fromMessage(message);
            String sign = SignUtil.getPaySign(map, NetSchoolConfig.API_KEY);
            map.put("sign",sign);
            userServiceV3.updateNickname(RequestUtil.encrypt(map));
        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }
}
