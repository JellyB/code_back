package top.jbzm.index;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author jbzm
 * @date Create on 2018/4/3 10:26
 */
@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableEurekaClient
public class IndexApplication {
    public static void main(String[] args) {
        MDC.put("lol","ll");
        SpringApplication.run(IndexApplication.class, args);
    }
}
