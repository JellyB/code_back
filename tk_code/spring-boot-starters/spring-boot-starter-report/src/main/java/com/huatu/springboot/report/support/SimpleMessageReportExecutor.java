package com.huatu.springboot.report.support;

import com.huatu.common.spring.executor.NamedThreadPoolTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author hanchao
 * @date 2018/1/11 15:45
 */
@Slf4j
public class SimpleMessageReportExecutor extends NamedThreadPoolTaskExecutor implements MessageReportExecutor {

    public SimpleMessageReportExecutor(){
        super("reportThreadPool");
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
