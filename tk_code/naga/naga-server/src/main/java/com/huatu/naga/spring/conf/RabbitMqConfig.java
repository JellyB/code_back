package com.huatu.naga.spring.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.naga.mq.listeners.ReportMessageListener;
import com.huatu.tiku.common.consts.RabbitConsts;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author hanchao
 * @date 2017/9/4 14:05
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper){
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * queue声明
     * @return
     */
    @Bean
    public Queue sendFreeCourseQueue(){
        return new Queue(RabbitConsts.QUEUE_REPORT);
    }


    @Bean
    public SimpleMessageListenerContainer rewardMessageListenerContainer(@Autowired ConnectionFactory connectionFactory,
                                                                         @Autowired(required = false) @Qualifier("coreThreadPool") ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                                                         @Autowired ReportMessageListener reportMessageListener,
                                                                         @Autowired AmqpAdmin amqpAdmin){
        SimpleMessageListenerContainer manualRabbitContainer = new SimpleMessageListenerContainer();
        manualRabbitContainer.setQueueNames(RabbitConsts.QUEUE_REPORT);
        manualRabbitContainer.setConnectionFactory(connectionFactory);
        if(amqpAdmin instanceof RabbitAdmin){
            manualRabbitContainer.setRabbitAdmin((RabbitAdmin) amqpAdmin);
        }
        if(threadPoolTaskExecutor != null){
            manualRabbitContainer.setTaskExecutor(threadPoolTaskExecutor);
        }
        manualRabbitContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        manualRabbitContainer.setConcurrentConsumers(10);
        manualRabbitContainer.setMaxConcurrentConsumers(20);
        manualRabbitContainer.setMessageListener(reportMessageListener);
        return manualRabbitContainer;
    }


}
