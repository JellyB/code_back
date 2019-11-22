package com.huatu.tiku.course.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.spring.conventer.FormMessageConverter;
import com.huatu.springboot.report.product.extend.EnableWebResultHolder;
import feign.Logger;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author hanchao
 * @date 2017/8/18 15:47
 */
@Configuration
@EnableApolloConfig
@EnableAsync
@EnableCircuitBreaker
@EnableScheduling
@EnableWebResultHolder
public class SpringBaseConfig {

    /**
     * 支持map转url encode，可以用于post body而不是放在url上
     * feign decoder默认使用了全局的httpmessageconverters
     * @return
     */
    @Bean
    public FormMessageConverter formMessageConverter(){
        return new FormMessageConverter();
    }

    @Bean
    //@Profile("")
    public Logger.Level feignLoggerLevel(ConfigurableEnvironment environment){
        if(environment.acceptsProfiles("product")){ //生产环境使用基本日志，否则使用完整日志
            return Logger.Level.BASIC;
        }else{
            return Logger.Level.FULL;
        }
    }

}
