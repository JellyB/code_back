package com.arj.monitor.aop.session;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author zhouwei
 * @Description: token
 * @create 2018-10-15 上午11:04
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Token {
    String DEFAULT_NAME = "token";

    @AliasFor("name")
    String value() default DEFAULT_NAME;

    @AliasFor("value")
    String name() default DEFAULT_NAME;

    /**
     * required和spring集成后不代表头信息不存在，而是说最后的值存不存在，所以是uid，或者user bean,设置defaultValue后可以绕过这个为null的报错
     * default的字符串对于基本类型的转换
     * simple类型默认返回-1不受此限制
     * @return
     */
    boolean required() default true;

    /**
     * 验证session信息,会调用assert方法,可能会抛出异常
     * @return
     */
    boolean check() default true;

    String defaultValue() default "";
}
