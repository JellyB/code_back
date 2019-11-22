package com.huatu.tiku.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hanchao
 * @date 2018/1/3 17:39
 */
@ComponentScan(basePackageClasses = SearchApplication.class)
@EnableAutoConfiguration
@SpringBootConfiguration
public class SearchApplication {
    public static void main(String[] args){
        SpringApplication.run(SearchApplication.class, args);
    }
}
