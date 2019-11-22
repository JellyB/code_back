package com.huatu.tiku.interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @Author jbzm
 * @Date Create on 2018/1/23 14:22
 */
@Configuration
public class SpringBean {

    /**
     * 声明什么鬼,没他用不了  MultipartFile
     *
     * @return
     */
    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        //resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setResolveLazily(true);
        //上传文件大小 50M 50*1024*1024
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(50 * 1024 * 1024);
        return resolver;
    }
}
