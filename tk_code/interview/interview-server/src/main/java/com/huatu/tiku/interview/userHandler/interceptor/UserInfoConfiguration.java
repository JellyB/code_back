package com.huatu.tiku.interview.userHandler.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by junli on 2018/4/11.
 */
@Configuration
public class UserInfoConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public UserInfoInterceptor userInfoInterceptor() {
        return new UserInfoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoInterceptor())
                .addPathPatterns("/**") //匹配规则
                .excludePathPatterns("/login");//排除规则
        super.addInterceptors(registry);
    }
}
