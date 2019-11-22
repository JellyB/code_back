package com.huatu.tiku.correct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-22  17:28 .
 */
@ComponentScan("com.huatu.tiku.correct")
@EnableAutoConfiguration
@EnableFeignClients  // feign如果要做额外的配置，不能处于该包下，不能被component扫描到
public class CorrectAppliaction {
    public static void main(String[] args) {
        SpringApplication.run(CorrectAppliaction.class,args);
    }
}
