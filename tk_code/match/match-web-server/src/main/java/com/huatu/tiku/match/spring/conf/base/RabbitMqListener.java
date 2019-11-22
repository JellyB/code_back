package com.huatu.tiku.match.spring.conf.base;


import com.huatu.tiku.match.listener.ListenerService;
import com.huatu.tiku.match.listener.enums.RabbitMatchKeyEnum;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by lijun on 2018/8/10
 */
@Slf4j
@Configuration
@ConditionalOnClass(RabbitMqConfig.class)
@AutoConfigureAfter(RabbitMqConfig.class)
public class RabbitMqListener implements ApplicationContextAware {
    @Value("${spring.profiles}")
    public String env;

    private static ApplicationContext applicationContext;


    @Bean
    public MessageListenerContainer messageListenerContainer(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        //RabbitMatchKeyEnum存储所有的消息队列名称前缀和相应的listener
        RabbitMatchKeyEnum[] values = RabbitMatchKeyEnum.values();
        String[] queues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            queues[i] = RabbitMatchKeyEnum.getQueue(values[i], env);
        }
        container.setQueueNames(queues);
        container.setConnectionFactory(connectionFactory);
        // 设置消费者线程数
        container.setConcurrentConsumers(5);
        // 设置最大消费者线程数
        container.setMaxConcurrentConsumers(10);
        container.setMessageListener((MessageListener) message -> {
            try {
                String s = new String(message.getBody(), "UTF-8");
                Map map = JsonUtil.toMap(s);
                String consumerQueue = message.getMessageProperties().getConsumerQueue();
                RabbitMatchKeyEnum rabbitMatchKeyEnum = RabbitMatchKeyEnum.create(consumerQueue);
                log.info("consumerQueue={},message={}", consumerQueue, map);
                if(null == rabbitMatchKeyEnum){
                    log.error("队列consumerQueue={}匹配失败",consumerQueue);
                    return;
                }
                ListenerService bean = (ListenerService) applicationContext.getBean(rabbitMatchKeyEnum.getListenerClass());
                bean.onMessage(map);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        return container;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
