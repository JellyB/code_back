package com.huatu.tiku.config.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.spring.conventer.FormMessageConverter;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * @author hanchao
 * @date 2017/8/18 15:47
 */
@Configuration
@EnableApolloConfig
@EnableAsync
@EnableScheduling
public class SpringBaseConfig {

    /**
     * 支持map转url encode，可以用于post body而不是放在url上
     * feign decoder默认使用了全局的httpmessageconverters
     * @return
     */
    @Bean
    public FormMessageConverter formMessageConverter(){
        return new FormMessageConverter();
    }

//    @Bean
//    public MapperScannerConfigurer mapperScannerConfigurer(){
//        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
//        mapperScannerConfigurer.setBasePackage("com.huatu.tiku.teacher.dao");
//        mapperScannerConfigurer.setSqlSessionFactoryBeanName("teacherSqlSessionFactory");
//        return mapperScannerConfigurer;
//    }

    @Bean
    public HTMultipartResolver htMultipartResolver(){
        HTMultipartResolver commonsMultipartResolver = new HTMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSize(10240000);
        commonsMultipartResolver.setMaxInMemorySize(2048);
        commonsMultipartResolver.setExcludeUrls("/upload,/edit,/pand");
        return commonsMultipartResolver;
    }

    @Bean
    public Logger.Level feignLoggerLevel(ConfigurableEnvironment environment){
        if(environment.acceptsProfiles("product")){
            return Logger.Level.BASIC;
        }else{
            return Logger.Level.FULL;
        }
    }
}
