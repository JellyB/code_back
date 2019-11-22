package com.huatu.tiku.interview.spring.conf.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * @author hanchao
 * @date 2017/9/19 12:22
 */
@Configuration
public class WebConfig {
    /**
     * 方法级别的验证
     * @return
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

}
