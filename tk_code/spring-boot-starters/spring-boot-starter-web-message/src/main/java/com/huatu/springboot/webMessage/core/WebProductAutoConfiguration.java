package com.huatu.springboot.webMessage.core;

import com.huatu.springboot.webMessage.product.WebMessageAdapter;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 配置全局的过滤器,用以拦截web请求信息,用作数据上报使用
 * Created by junli on 2018/4/8.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(UserSessionService.class)//必须使用 start-user 组件
@ConditionalOnProperty(name = "enable", prefix = "tiku.web.message",matchIfMissing = true)//全局配置 tiku.web.message = enable 生效
public class WebProductAutoConfiguration extends WebMvcConfigurerAdapter {

    //拦截器规则
    @Autowired
    private MessageReportProperties properties;

    //拦截器
    @Bean
    public WebMessageAdapter webMessageAdapter() {
        return new WebMessageAdapter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration patterns = registry.addInterceptor(webMessageAdapter())
                .addPathPatterns(properties.getWeb().getMatches());//添加匹配规则
        if (null != properties.getWeb().getExcludes() && properties.getWeb().getExcludes().length > 0) {
            patterns.excludePathPatterns(properties.getWeb().getExcludes());//添加排除规则
        }
        super.addInterceptors(registry);
    }

}
