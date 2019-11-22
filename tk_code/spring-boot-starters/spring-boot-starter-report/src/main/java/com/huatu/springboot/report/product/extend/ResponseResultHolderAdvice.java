package com.huatu.springboot.report.product.extend;

import com.huatu.springboot.report.annotation.WebReport;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author hanchao
 * @date 2018/3/28 17:09
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ResponseResultHolderAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        WebReport webReport = returnType.getMethodAnnotation(WebReport.class);
        return webReport == null ? false : webReport.holdResult();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ResponseResultHolder.set(body);
        return body;
    }
}
