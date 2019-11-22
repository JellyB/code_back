package com.huatu.springboot.report.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.springboot.report.support.MessageReportExecutor;
import com.huatu.springboot.report.support.RabbitReporter;
import com.huatu.springboot.report.support.SimpleMessageReportExecutor;
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
 * @author hanchao
 * @date 2018/1/11 15:36
 */
@Configuration
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@AutoConfigureBefore({WebProducterAutoConfiguration.class,ExceptionProducterAutoConfiguration.class})
@EnableConfigurationProperties(MessageReportProperties.class)
public class ReportCoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Jackson2JsonMessageConverter.class)
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper){
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    @Bean
    @ConditionalOnMissingBean(MessageReportExecutor.class)
    public SimpleMessageReportExecutor simpleMessageReportExecutor(){
        SimpleMessageReportExecutor simpleMessageReportExecutor = new SimpleMessageReportExecutor();
        simpleMessageReportExecutor.setCorePoolSize(5);
        simpleMessageReportExecutor.setAllowCoreThreadTimeOut(true);//允许核心线程超时销毁
        simpleMessageReportExecutor.setKeepAliveSeconds(60);
        simpleMessageReportExecutor.setMaxPoolSize(20);
        simpleMessageReportExecutor.setQueueCapacity(10000);
        return simpleMessageReportExecutor;
    }

    @Configuration
    @ConditionalOnClass(RabbitTemplate.class)
    protected class RabbitReporterConfig{
        @Bean
        @ConditionalOnBean(RabbitTemplate.class)
        public RabbitReporter rabbitReporter(){
            return new RabbitReporter();
        }
    }

}
