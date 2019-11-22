package com.huatu.springboot.webMessage.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.springboot.webMessage.support.WebMessageReportExecutor;
import com.huatu.springboot.webMessage.support.impl.RabbitMqReport;
import com.huatu.springboot.webMessage.support.impl.RabbitMqReportExecutor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by junli on 2018/4/10.
 */
@Configuration
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@AutoConfigureBefore({WebProductAutoConfiguration.class})
@EnableConfigurationProperties(MessageReportProperties.class)
public class DefaultReportConfiguration {

    @Bean
    @ConditionalOnMissingBean(Jackson2JsonMessageConverter.class)
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 未设置上报的任务处理策略,使用默认策略
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(WebMessageReportExecutor.class)
    public RabbitMqReportExecutor simpleMessageReportExecutor() {
        RabbitMqReportExecutor rabbitMqReportExecutor = new RabbitMqReportExecutor();
        rabbitMqReportExecutor.setCorePoolSize(5);
        rabbitMqReportExecutor.setAllowCoreThreadTimeOut(true);//允许核心线程超时销毁
        rabbitMqReportExecutor.setKeepAliveSeconds(60);
        rabbitMqReportExecutor.setMaxPoolSize(20);
        rabbitMqReportExecutor.setQueueCapacity(10000);
        return rabbitMqReportExecutor;
    }

    /**
     * 为设置上报策略,使用默认策略
     */
    @Configuration
    @ConditionalOnClass(RabbitTemplate.class)
    protected class RabbitReporterConfig {
        @Bean
        @ConditionalOnBean(RabbitTemplate.class)
        public RabbitMqReport rabbitReporter() {
            return new RabbitMqReport();
        }
    }

}
