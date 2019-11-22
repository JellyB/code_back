package com.huatu.tiku.course.netschool.api.v5.configuration;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import feign.Feign;
import feign.Target;
import feign.hystrix.SetterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * Created by lijun on 2018/11/28
 */
@Configuration
public class CourseServiceV5Config {

    private static int connectTimeOutMillis = 20 * 1000;

    @Bean
    public SetterFactory create() {
        return (Target<?> target, Method method) -> {
            String groupKey = target.name();
            String commandKey = Feign.configKey(target.type(), method);
            if (method.getName().equals("findPurchasesTimetable")) {
                //课程大纲-售后
                return HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey + "_self"))
                        .andCommandPropertiesDefaults(
                                HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(connectTimeOutMillis) // 超时配置
                        );
            }
            return HystrixCommand.Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey));

        };
    }

}
