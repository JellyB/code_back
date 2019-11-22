package com.huatu.springboot.report.core;

import com.huatu.springboot.report.product.ExceptionProducter;
import com.huatu.springboot.report.support.MessageReporter;
import com.huatu.springboot.web.tools.exception.ExceptionHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanchao
 * @date 2018/1/16 16:23
 */
@Configuration
@AutoConfigureBefore(ExceptionHandlerAutoConfiguration.class)
@ConditionalOnClass(ExceptionHandlerAutoConfiguration.class)
public class ExceptionProducterAutoConfiguration {
    @Bean
    @ConditionalOnBean(MessageReporter.class)
    public ExceptionProducter exceptionProducter(){
        return new ExceptionProducter();
    }
}
