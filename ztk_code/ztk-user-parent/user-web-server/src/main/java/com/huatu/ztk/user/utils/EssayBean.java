package com.huatu.ztk.user.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class EssayBean {
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION,proxyMode = ScopedProxyMode.INTERFACES)
    public RestTemplate restTemplate(){
        ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(clientHttpRequestFactory);
    }
}
