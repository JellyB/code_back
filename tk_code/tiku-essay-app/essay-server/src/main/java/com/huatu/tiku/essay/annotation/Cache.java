package com.huatu.tiku.essay.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 15:52
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD,ElementType.TYPE})
public @interface Cache {
    /**
     * this is key
     * @return
     */
    String key() default "";

    /**
     * this is scope
     * @return
     */
    CacheScope scope() default CacheScope.application;

    /**
     * this is expire
     * @return
     */
    int expire() default 720;

    /**
     * this is desc~~
     * @return
     */
    String desc() default "";

}
