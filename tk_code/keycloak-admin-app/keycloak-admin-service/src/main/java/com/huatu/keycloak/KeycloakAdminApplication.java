package com.huatu.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hanchao
 * @date 2017/10/17 13:46
 */
@ComponentScan("com.huatu.keycloak")
@EnableAutoConfiguration
@EnableDiscoveryClient
@SpringBootConfiguration
public class KeycloakAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(KeycloakAdminApplication.class, args);
    }
}
