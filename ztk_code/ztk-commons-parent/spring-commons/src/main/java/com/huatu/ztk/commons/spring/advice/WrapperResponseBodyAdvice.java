package com.huatu.ztk.commons.spring.advice;

import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.Result;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.commons.exception.SuccessResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-04-18 09:56
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class WrapperResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (isHtml(mediaType)) {
            return o;
        }

        if (o == null) {//没有返回数据
            return SuccessMessage.create("操作成功");
        }else if(o instanceof Result){//已经是封装后的result
            //会话过期,把http状态码设置为401
            final Result result = (Result) o;
            //用户session过期||其他设备登录
            if (result.getCode() == CommonErrors.SESSION_EXPIRE.getCode()
                    || result.getCode() == CommonErrors.LOGIN_ON_OTHER_DEVICE.getCode()) {
                serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            }
            return o;
        }
        return new SuccessResponse(o);
    }

    /**
     * 返回是否是html
     * @param mediaType
     * @return
     */
    public boolean isHtml(MediaType mediaType){
        return mediaType.includes(MediaType.TEXT_HTML);
    }

}
