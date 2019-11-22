package com.huatu.springboot.webMessage.annotation;

import com.huatu.springboot.webMessage.core.RabbitMqReportQueueEnum;

import java.lang.annotation.*;

/**
 *
 * Created by junli on 2018/4/8.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WebReport {

    /**
     * 上报队列中的数据名称
     * @return
     */
    String value() default "";

    /**
     * 数据生成器
     * @return
     */
    Class<?> extraHandler() default Object.class;

    /**
     * 获取方式 ResponseResultHolder.get();
     * 是否缓存返回的结果集在ThreadLocal 中
     */
    boolean holdResult() default false;
    /**
     * 上报队列信息
     */
    RabbitMqReportQueueEnum[] queueName() default RabbitMqReportQueueEnum.QUEUE_REPORT;
}
