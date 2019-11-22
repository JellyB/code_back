package com.huatu.springboot.report.core;

import com.huatu.springboot.report.product.WebMessageProducter;
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
 * @author hanchao
 * @date 2018/1/12 10:29
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(UserSessionService.class)
@ConditionalOnProperty(name = "enable",prefix = "tiku.report.web",matchIfMissing = true)
public class WebProducterAutoConfiguration {

    @Bean
    public WebMessageProducter webMessageProducter(){
        return new WebMessageProducter();
    }

    @Configuration
    protected static class WebProducterConfiguration extends WebMvcConfigurerAdapter {
        @Autowired
        private MessageReportProperties properties;
        @Autowired
        private WebMessageProducter webMessageProducter;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            InterceptorRegistration webReportInterceptor = registry.addInterceptor(webMessageProducter)
                    .addPathPatterns(properties.getWeb().getMatches());
            if(properties.getWeb().getExcludes() != null && properties.getWeb().getExcludes().length > 0){
                webReportInterceptor.excludePathPatterns(properties.getWeb().getExcludes());
            }
            super.addInterceptors(registry);
        }
    }
}
