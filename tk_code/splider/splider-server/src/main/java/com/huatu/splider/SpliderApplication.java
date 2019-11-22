package com.huatu.splider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hanchao
 * @date 2018/2/22 15:50
 */
@ComponentScan(basePackageClasses = SpliderApplication.class)
@EnableAutoConfiguration
@SpringBootConfiguration
@EnableScheduling
public class SpliderApplication {
    public static void main(String[] args){
        SpringApplication.run(SpliderApplication.class, args);
    }
}
