package com.huatu.tiku.essay.web.controller.tool;

import com.google.common.collect.Maps;
import com.huatu.tiku.essay.mq.listeners.ManualCorrectFinishListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author huangqingpeng
 * @title: RabbitUtilController
 * @description: TODO
 * @date 2019-07-2411:56
 */
@RestController
@RequestMapping("rabbit")
public class RabbitUtilController {

    @Autowired
    ManualCorrectFinishListener manualCorrectFinishListener;
    @Autowired
    private MessageConverter messageConverter;
    /**
     * 完成批改
     */
    @RequestMapping("correct/finish")
    public void correctFinish(int answerId,int answerType){
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("answerId",answerId);
        map.put("answerType",answerType);
        Message message = messageConverter.toMessage(map,new MessageProperties());
        manualCorrectFinishListener.onMessage(message);
    }
}
