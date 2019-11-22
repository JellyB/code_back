package com.huatu.tiku.essay.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 16:28
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface CacheClear {

    String pre() default "";
    String key() default "";
    String[] keys() default "";
//    Class<? extends >
}
