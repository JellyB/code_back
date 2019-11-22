package com.huatu.springboot.webMessage.annotation;

import com.huatu.springboot.webMessage.product.ResponseResultHolderAdvice;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by junli on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ResponseResultHolderAdvice.class)
public @interface EnableWebResultHolder {
}
