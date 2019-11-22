package com.huatu.springboot.webMessage.support.impl;

import com.huatu.common.spring.executor.NamedThreadPoolTaskExecutor;
import com.huatu.springboot.webMessage.support.WebMessageReportExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by junli on 2018/4/10.
 */
@Slf4j
public class RabbitMqReportExecutor extends NamedThreadPoolTaskExecutor implements WebMessageReportExecutor {

    public RabbitMqReportExecutor() {
        super("webMessageReport");
    }

    @Override
    protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        return super.initializeExecutor(threadFactory, new ThreadPoolExecutor.AbortPolicy(){
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                getThreadPoolExecutor().getQueue().clear();//清空队列，防止堆积
                log.error("reject task,{}",e);
            }
        });
    }
}
