package com.huatu.springboot.degrade.support;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.springboot.degrade.core.DegradeConsts;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author hanchao
 * @date 2017/10/16 17:10
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
@Slf4j
public class DegradeAspect {
    @ApolloConfig("degrade")
    private Config config;


    @Pointcut("execution(* *..*(..)) && @annotation(com.huatu.springboot.degrade.core.Degrade)")
    private void degradeMethod() {
    }


    @Around("degradeMethod()")
    public Object doArround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Object target = pjp.getTarget();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        Degrade degrade = method.getAnnotation(Degrade.class);
        String switchStatus = null;
        try {
            switchStatus = config.getProperty(degrade.key(), DegradeConsts.CLOSED);
        } catch(Exception e){
            log.error("get config from apollo cause an error.",e);
            e.printStackTrace();
        }
        if (! DegradeConsts.OPEN.equals(switchStatus)) {
            return pjp.proceed();
        }
        String degradeMethod = degrade.method();
        if(StringUtils.isEmpty(degradeMethod)){
            degradeMethod = method.getName() + DegradeConsts.METHOD_SUFFIX;
        }
        try {
            Method degradeMethodInvoke = method.getDeclaringClass().getMethod(degradeMethod,method.getParameterTypes());
            return degradeMethodInvoke.invoke(target,args);
        } catch(NoSuchMethodException | SecurityException e){
            return handleAutoDegrade(method);
        }
    }

    private Object handleAutoDegrade(Method method){
        Class<?> returnType = method.getReturnType();
        if(! returnType.isPrimitive()){
            return null;
        }
        if(returnType.equals(Void.TYPE)){
            return null;
        }
        if(returnType.equals(Boolean.TYPE)){
            return false;
        }
        if(returnType.equals(Character.TYPE)){
            return ' ';
        }
        return 0;
    }
}
