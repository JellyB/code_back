package com.huatu.tiku.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jbzm
 * @date Create on 2018/3/6 21:20
 */
@ComponentScan(basePackageClasses = ScheduleApplication.class)
@SpringBootApplication
public class ScheduleApplication {
    public static void main(String[] args){
        SpringApplication.run(ScheduleApplication.class, args);
    }
}