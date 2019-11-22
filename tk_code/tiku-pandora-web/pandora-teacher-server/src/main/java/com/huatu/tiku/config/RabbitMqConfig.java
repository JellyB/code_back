package com.huatu.tiku.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.tiku.constants.RabbitKeyConstant;
import lombok.Builder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.huatu.tiku.constants.RabbitKeyConstant.*;

/**
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

    @Bean
    public Queue teacherKnowledgeQueue() {
        return new Queue("teacherKnowledge");
    }

    /**
     * 将mongo中试题数据同步到mysql中
     *
     * @return
     */
    @Bean
    public Queue syncQuestion2DBQueue() {
        return new Queue(RabbitKeyConstant.getQuestion_2_mysql(env), false, false, false, null);
    }

    /**
     * 将mysql中试题同步到mongo库ztk_question_new表中
     *
     * @return
     */
    @Bean
    public Queue syncQuestion2MongoQueue() {
        return new Queue(RabbitKeyConstant.getQuestion_2_mongo(env), false, false, false, null);
    }

    /**
     * 试题根据试卷同步到mysql的实现逻辑
     *
     * @return
     */
    @Bean
    public Queue syncQuestionByPaperQueue() {
        return new Queue(SyncQuestionByPaper, false, false, false, null);
    }
    /**
     * 试题绑定关系同步队列
     *
     * @return
     */
    @Bean
    public Queue syncPaperQuestionQueue() {
        return new Queue(SyncPaperQuestion, false, false, false, null);
    }

    /**
     * 错题下载异步实现
     * @return
     */
    @Bean
    public Queue questionErrorDownloadTask() {
        return new Queue(RabbitKeyConstant.getErrorDownload(env), false, false, false, null);
    }
}
