package com.huatu.tiku.course.spring.conf.base;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域请求配置
 * Created by lijun on 2018/7/24
 */
@Configuration
public class CorsConfig {

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");//域名
        corsConfiguration.addAllowedHeader("*");//头信息
        corsConfiguration.addAllowedMethod("*");//方法类型
        return corsConfiguration;
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", buildConfig());

        //设置成第一个
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new CorsFilter(urlBasedCorsConfigurationSource));
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }
}
