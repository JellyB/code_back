package com.huatu.tiku.springboot.users.support;

import com.huatu.tiku.springboot.users.core.UsersAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2017/4/23 17:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(UsersAutoConfiguration.class)

@Deprecated
public @interface EnableUserSessions {

}
