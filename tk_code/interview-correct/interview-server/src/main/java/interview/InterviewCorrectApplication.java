package interview;

import com.huatu.tiku.springboot.basic.support.BasicInfoAutoconfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hanchao
 * @date 2017/11/22 16:45
 */

@ComponentScan(basePackageClasses = InterviewCorrectApplication.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, BasicInfoAutoconfiguration.class})
@SpringBootApplication
@Slf4j
public class InterviewCorrectApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewCorrectApplication.class, args);
    }
}
