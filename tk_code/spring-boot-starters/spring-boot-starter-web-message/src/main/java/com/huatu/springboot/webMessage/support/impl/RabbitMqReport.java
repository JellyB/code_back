package com.huatu.springboot.webMessage.support.impl;

import com.huatu.springboot.webMessage.support.WebMessageReport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by junli on 2018/4/10.
 */
public class RabbitMqReport implements WebMessageReport {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    public void report(String queueName, Object object) {
        rabbitTemplate.send(queueName, getMessage(object));
    }

    private final Message getMessage(Object message) {
        return jackson2JsonMessageConverter.toMessage(message, new MessageProperties());
    }
}
