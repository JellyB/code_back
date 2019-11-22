package com.huatu.springboot.webMessage.support;

/**
 * 上报业务执行策略
 * Created by junli on 2018/4/10.
 */
public interface WebMessageReportExecutor {
    void execute(Runnable runnable);
}
