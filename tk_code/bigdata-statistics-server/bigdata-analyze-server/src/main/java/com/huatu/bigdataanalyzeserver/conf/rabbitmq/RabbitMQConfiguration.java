package com.huatu.bigdataanalyzeserver.conf.rabbitmq;

import com.huatu.bigdataanalyzecommon.constant.RabbitmqConstant;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    public final static String QUEUE_NAME = RabbitmqConstant.RABBIT_TOPIC_RECORD_QUEUE_NAME;

    @Bean
    public ConnectionFactory createRabbitmqConnection() {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost(RabbitmqConstant.RABBIT_HOST);
        factory.setUsername(RabbitmqConstant.RABBIT_USERNAME);
        factory.setPassword(RabbitmqConstant.RABBIT_PASSWORD);

        return factory;
    }
}
