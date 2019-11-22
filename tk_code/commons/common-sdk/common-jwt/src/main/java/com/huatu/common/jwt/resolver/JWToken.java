package com.huatu.common.jwt.resolver;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2018/3/26 17:05
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JWToken {
    String DEFAULT_NAME = "jtoken";

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

    String defaultValue() default "";

    boolean verify() default true;
}
