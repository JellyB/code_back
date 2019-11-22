package com.huatu.springboot.web.tools.exception;

import com.huatu.common.CommonResult;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.springboot.web.tools.indicator.ExceptionHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hanchao
 * @date 2018/1/18 21:34
 */
@ConditionalOnProperty(value = "htonline.ex-handler.enabled",havingValue = "true",matchIfMissing = true)
public class ExceptionHandlerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ErrorResultHandler.class)
    public ErrorResultHandler errorResultHandler(){
        return new SimpleErrorResultHandler();
    }

    @Bean
    public BizExceptionResolver bizExceptionResolver(){
        return new BizExceptionResolver();
    }


    @Bean
    @ConditionalOnMissingBean(ExceptionCounter.class)
    public ExceptionCounter exceptionCounter(){
        return new SimpleExceptionWindowCounter();
    }

    @Bean
    public ExceptionHealthIndicator exceptionHealthIndicator(){
        return new ExceptionHealthIndicator();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(){
        return new GlobalExceptionHandler();
    }

    static class BizExceptionResolver implements ExceptionResolver {

        @Override
        public ErrorResult resolve(Exception ex, HttpServletRequest request) {
            if(ex instanceof BizException && ((BizException) ex).getCustomMessage() != null){
                ErrorResult optional = ((BizException) ex).getErrorResult();
                return optional == null ? CommonResult.SERVICE_INTERNAL_ERROR : ErrorResult.create(optional.getCode(),((BizException) ex).getCustomMessage());
            }
            return null;
        }

        @Override
        public boolean canResolve(Exception ex,HttpStatus httpstatus) {
            if(ex instanceof BizException && ((BizException) ex).getCustomMessage() != null){
                return true;
            }
            return false;
        }
    }
}
