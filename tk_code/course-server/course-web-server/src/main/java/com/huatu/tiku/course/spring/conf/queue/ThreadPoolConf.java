package com.huatu.tiku.course.spring.conf.queue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-18 上午11:22
 **/
@Configuration
@Component
@Slf4j
public class ThreadPoolConf {


    @Bean
    public ThreadFactory threadFactory(){
        return new ThreadFactoryBuilder().setNameFormat("course-pool-%s").build();
    }

    @Bean(value = "courseExecutorService")
    public ExecutorService executorService(){
        ExecutorService pool = new ThreadPoolExecutor(5, 50, 200,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1024),
                threadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        return pool;
    }
}
