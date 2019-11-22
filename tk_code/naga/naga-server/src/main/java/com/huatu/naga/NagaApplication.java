package com.huatu.naga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hanchao
 * @date 2018/1/18 14:54
 */
@ComponentScan(basePackageClasses = NagaApplication.class)
@EnableAutoConfiguration
@SpringBootConfiguration
public class NagaApplication {
    public static void main(String[] args) {
        SpringApplication.run(NagaApplication.class, args);
    }
}
