package com.huatu.tiku.match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

/**
 * Created by lijun on 2018/10/10
 */
@ComponentScan(basePackageClasses = MatchApplication.class)
@EnableAutoConfiguration
@EnableFeignClients  // feign如果要做额外的配置，不能处于该包下，不能被component扫描到
@SpringBootConfiguration
@RestController
public class MatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchApplication.class, args);
    }

    //服务检测接口
    @GetMapping
    public Object check(){
        return LocalTime.now().toString();
    }
}
