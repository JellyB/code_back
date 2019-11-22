package com.huatu.springboot.report.annotation;

import com.huatu.springboot.report.core.RabbitMqReportQueueEnum;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2018/1/15 10:39
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WebReport {
    String value() default "";
    Class<?> extraHandler() default Object.class;

    /**
     * 获取方式 ResponseResultHolder.get();
     * 是否缓存返回的结果集在ThreadLocal 中
     */
    boolean holdResult() default false;
    /**
     * 连接名称
     */
    RabbitMqReportQueueEnum[] queueName() default RabbitMqReportQueueEnum.QUEUE_REPORT;

}
