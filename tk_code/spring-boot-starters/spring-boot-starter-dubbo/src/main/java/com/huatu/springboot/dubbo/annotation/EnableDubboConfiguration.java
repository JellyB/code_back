package com.huatu.springboot.dubbo.annotation;

import java.lang.annotation.*;

/**
 * Enable Dubbo (for provider or consumer) for spring boot application
 *
 * @author xionghui
 * @email xionghui.xh@alibaba-inc.com
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableDubboConfiguration {

}
