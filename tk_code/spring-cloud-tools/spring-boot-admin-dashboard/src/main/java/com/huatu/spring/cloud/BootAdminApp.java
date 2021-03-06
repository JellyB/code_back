package com.huatu.spring.cloud;

import de.codecentric.boot.admin.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

/**
 * @author hanchao
 * @date 2017/10/2 10:15
 */
@SpringBootApplication
@EnableTurbine
@EnableAdminServer
@EnableDiscoveryClient
public class BootAdminApp {
    public static void main(String[] args){
        SpringApplication.run(BootAdminApp.class,args);
    }
}
