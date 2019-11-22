package com.huatu.tiku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableFeignClients
@MapperScan("com.huatu.tiku.mapper")
public class TikuApplication {


	public static void main(String[] args) {
		SpringApplication.run(TikuApplication.class, args);
	}
}
