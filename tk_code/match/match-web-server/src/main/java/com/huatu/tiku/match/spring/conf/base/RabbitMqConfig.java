package com.huatu.tiku.match.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.tiku.match.listener.enums.RabbitMatchKeyEnum;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Rabbit 配置
 * 由于测试环境 和 开发环境共用一个 rabbitMQ 此处队列声明建议加上 环境信息
 * @author hanchao
 * @date 2017/9/4 14:05
 */
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_RABBIT)
@Configuration
public class RabbitMqConfig {

    @Value("${spring.profiles}")
    public String env;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean("rabbitFactory")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(60);
        factory.setConcurrentConsumers(20);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    /**
     * 模考大赛用户数据同步
     * @return
     */
    @Bean
    public Queue matchUserMetaSyncQueue(){
        return new Queue(RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.MatchUserMetaSync,env), false, false, false, null);
    }
    /**
     * 模考大赛试题数据同步
     * @return
     */
    @Bean
    public Queue matchQuestionMetaSyncQueue(){
        return new Queue(RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.MatchQuestionMetaSync,env), false, false, false, null);
    }

    /**
     * 处理模考大赛答题卡交卷信息
     * @return
     */
    @Bean
    public Queue answerCardSubmitAsyncQueue(){
        return new Queue(RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.AnswerCardSubmitAsync,env), false, false, false, null);
    }

}

