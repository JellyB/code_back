package com.huatu.sensors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages= {"com.huatu.ztk.*","com.huatu.sensors.*"})
public class SensorsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorsApplication.class, args);
	}

}

