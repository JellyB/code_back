package com.huatu.springboot.report.product.extend;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2018/3/28 17:23
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ResponseResultHolderAdvice.class)
public @interface EnableWebResultHolder {
}
