package com.huatu.tiku.essay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hanchao
 * @date 2017/11/22 16:45
 */

@ComponentScan(basePackageClasses = EssayApplication.class)
@SpringBootApplication
public class EssayApplication {
    public static void main(String[] args){
        SpringApplication.run(EssayApplication.class, args);

    }
}
