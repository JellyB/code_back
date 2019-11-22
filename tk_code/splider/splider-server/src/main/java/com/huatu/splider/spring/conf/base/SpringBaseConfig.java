package com.huatu.splider.spring.conf.base;

import com.google.common.collect.Lists;
import com.huatu.splider.task.FbCourseTask;
import com.huatu.springboot.web.tools.advice.AdviceExcluder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author hanchao
 * @date 2017/8/18 15:47
 */
@Configuration
@EnableAsync
@EnableScheduling
public class SpringBaseConfig {

    @Bean
    public AdviceExcluder adviceExcluder(){
        return new AdviceExcluder(null, Lists.newArrayList("/17kjs/**"));
    }


    @Bean
    @Profile("product")//只在线上启用抓取数据
    public FbCourseTask fbCourseTask(){
        return new FbCourseTask();
    }
}
