package com.huatu.tiku.interview.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_RABBIT;

/**
 * @author hanchao
 * @date 2017/9/4 14:05
 */
@EnableApolloConfig(NAMESPACE_TIKU_RABBIT)
@Configuration
public class RabbitMqConfig {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper){
        return new Jackson2JsonMessageConverter(objectMapper);
    }



    /*@Bean
    public Exchange abcdExchange(){
        return new FanoutExchange("abcdex");
    }

    @Bean
    public Binding testbingding(@Autowired @Qualifier("abcdQueue")Queue queue,
                                @Autowired @Qualifier("abcdExchange")Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("abcd-route").noargs();
}*/
}
