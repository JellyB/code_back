package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.tiku.springboot.basic.reward.RewardActionExecutor;
import com.huatu.tiku.springboot.basic.reward.RewardActionService;
import com.huatu.tiku.springboot.basic.reward.event.DefaultRewardEventHandler;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEventHandler;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEventListener;
import com.huatu.tiku.springboot.basic.subject.SubjectService;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author hanchao
 * @date 2017/10/6 10:33
 */
@Configuration
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_BASIC)
@EnableConfigurationProperties(BasicProperties.class)
public class BasicInfoAutoconfiguration {
    private BasicProperties basicProperties;
    public BasicInfoAutoconfiguration(BasicProperties basicProperties){
        this.basicProperties = basicProperties;
    }


    @Bean
    public SubjectService subjectService() throws IOException {
        return new SubjectService(basicProperties.getSubjects());
    }

    @Bean
    public RewardActionService rewardAction() throws IOException {
        return new RewardActionService(basicProperties.getRewardActions());
    }


    @Bean
    public ConfigListenerAdapter configListenerAdapter(){
        ConfigListenerAdapter configListenerAdapter = new ConfigListenerAdapter();
        return configListenerAdapter;
    }


    @Configuration
    protected static class RewardAutoConfiguration{

        @Bean
        public DefaultRewardEventHandler defaultRewardEventHandler(){
            return new DefaultRewardEventHandler();
        }

        @Bean
        @ConditionalOnBean(RewardActionEventHandler.class)
        public RewardActionEventListener rewardActionEventListener(){
            return new RewardActionEventListener();
        }

        @Bean
        @ConditionalOnMissingBean(Jackson2JsonMessageConverter.class)
        public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper){
            return new Jackson2JsonMessageConverter(objectMapper);
        }


        //没有用
        @Bean
        public RewardActionExecutor rewardActionExecutor(){
            return new RewardActionExecutor();
        }
    }
}