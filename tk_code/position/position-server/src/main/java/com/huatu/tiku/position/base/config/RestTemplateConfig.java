package com.huatu.tiku.position.base.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author wangjian
 **/
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate getRestTemplate(){
//        return new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        return new RestTemplate();
    }
}
