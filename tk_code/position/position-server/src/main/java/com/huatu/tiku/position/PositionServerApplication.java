package com.huatu.tiku.position;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PositionServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PositionServerApplication.class, args);
	}
}
