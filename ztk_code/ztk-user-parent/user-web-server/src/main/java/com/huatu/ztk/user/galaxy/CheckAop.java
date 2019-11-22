package com.huatu.ztk.user.galaxy;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author jbzm
 * @date Create on 2018/3/19 16:29
 */
@Aspect
@Component
public class CheckAop {
    @Autowired
    private MqService mqService;

    @AfterReturning(returning = "token", pointcut = "execution(public * com.huatu.ztk.user.controller.UserSystemControllerV1.checkStatus(..))")
    public void checkForMq(String token) {
        try {
            mqService.send(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
