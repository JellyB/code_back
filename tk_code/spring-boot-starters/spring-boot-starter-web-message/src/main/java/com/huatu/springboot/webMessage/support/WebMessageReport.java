package com.huatu.springboot.webMessage.support;

/**
 * 上报业务策略
 * Created by junli on 2018/4/10.
 */
public interface WebMessageReport {
    void report(String queueName, Object object);
}
