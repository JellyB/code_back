package com.huatu.tiku.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackageClasses = InterviewApplication.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class,MultipartAutoConfiguration.class})
@EnableScheduling
@ServletComponentScan
public class InterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewApplication.class, args);
	}

//	@Bean
//	public MorningReadingPushRunner startupRunner(){
//		return new MorningReadingPushRunner();
//	}
//	@Bean
//	public GetReadingTableRunner getReading(){
//		return new GetReadingTableRunner();
//	}

}
