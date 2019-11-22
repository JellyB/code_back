package com.huatu.tiku.essay.util;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LogPrint {
String description() default "";
}
