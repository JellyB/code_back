package com.huatu.tiku.config;

import com.huatu.tiku.constants.RabbitKeyConstant;
import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.tiku.teacher.listener.*;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by lijun on 2018/8/10
 */
@Slf4j
@Configuration
@ConditionalOnClass(RabbitMqConfig.class)
@AutoConfigureAfter(RabbitMqConfig.class)
public class RabbitMqListener {
    @Value("${spring.profiles}")
    public String env;

    @Autowired
    SyncQuestion2DBListener syncQuestion2DBListener;
    @Autowired
    SyncQuestion2MongoListener syncQuestion2MongoListener;
    @Autowired
    SyncQuestionByPaperListener syncQuestionByPaperListener;
    @Autowired
    SyncPaperQuestionListener syncPaperQuestionListener;
    @Autowired
    QuestionErrorDownloadListener questionErrorDownloadListener;


    @Bean
    public MessageListenerContainer messageListenerContainer(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setQueueNames(RabbitKeyConstant.getQuestion_2_mysql(env),
                RabbitKeyConstant.getQuestion_2_mongo(env),
                RabbitKeyConstant.SyncQuestionByPaper,
                RabbitKeyConstant.SyncPaperQuestion,
                RabbitKeyConstant.getErrorDownload(env)
        );
        container.setConnectionFactory(connectionFactory);
        // 设置消费者线程数
        container.setConcurrentConsumers(5);
        // 设置最大消费者线程数
        container.setMaxConcurrentConsumers(10);
        container.setMessageListener((MessageListener) message -> {
            try {
                String s = new String(message.getBody(), "UTF-8");
                String consumerQueue = message.getMessageProperties().getConsumerQueue();
                System.out.println("consumerQueue="+consumerQueue);
                Function<String,Map> convertMap = (str->{
                    Map map = JsonUtil.toMap(str);
                    log.info("consumerQueue={},message={}",consumerQueue,str);
                    return map;
                });
                if (RabbitKeyConstant.getQuestion_2_mysql(env).equals(consumerQueue)){
                    syncQuestion2DBListener.onMessage(convertMap.apply(s));
                }else if (RabbitKeyConstant.getQuestion_2_mongo(env).equals(consumerQueue)) {
                    syncQuestion2MongoListener.onMessage(convertMap.apply(s));
                }else if(RabbitKeyConstant.SyncQuestionByPaper.equals(consumerQueue)){
                    syncQuestionByPaperListener.onMessage(convertMap.apply(s));
                }else if(RabbitKeyConstant.SyncPaperQuestion.equals(consumerQueue)){
                    syncPaperQuestionListener.onMessage(convertMap.apply(s));
                }else if(RabbitKeyConstant.getErrorDownload(env).equals(consumerQueue)){
                    QuestionErrorDownloadTask task = JsonUtil.toObject(s, QuestionErrorDownloadTask.class);
                    questionErrorDownloadListener.onMessage(task);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        return container;
    }

}
