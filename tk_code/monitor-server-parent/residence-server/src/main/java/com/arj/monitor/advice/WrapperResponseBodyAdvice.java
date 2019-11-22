package com.arj.monitor.advice;

import com.arj.monitor.common.BaseResult;
import com.arj.monitor.common.CommonResult;
import com.arj.monitor.exception.Result;
import com.arj.monitor.exception.SuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author zhouwei
 * @Description: 返回结果统一封装
 * @create 2018-10-16 上午11:04
 **/
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@ConditionalOnProperty(value = "rest.wrapper.enabled", havingValue = "true", matchIfMissing = true)
public class WrapperResponseBodyAdvice implements ResponseBodyAdvice {

    @Autowired
    private AdviceExcluder adviceExcluder;

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        if (isHtml(mediaType)) {
            return o;
        }
        //优先返回无需wrapper的，保证效率
        if(o != null && o instanceof Result ){
            return o;
        }else if(o != null && o instanceof BaseResult) {
        	//中间件状态结果不需要包装
        	 return o;
        }
        if(adviceExcluder.ignore(o,serverHttpRequest)){
            return o;
        }
        //最后进行包装
        return o == null ? CommonResult.SUCCESS : new SuccessResponse(o);
    }


    /**
     * 返回是否是html
     *
     * @param mediaType
     * @return
     */
    public boolean isHtml(MediaType mediaType) {
        return mediaType.includes(MediaType.TEXT_HTML);
    }





}
