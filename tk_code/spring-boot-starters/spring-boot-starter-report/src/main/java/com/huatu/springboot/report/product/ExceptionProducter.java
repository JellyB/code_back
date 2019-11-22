package com.huatu.springboot.report.product;

import com.huatu.common.ErrorResult;
import com.huatu.common.utils.date.TimestampUtil;
import com.huatu.springboot.report.support.MessageReportExecutor;
import com.huatu.springboot.report.support.RabbitReporter;
import com.huatu.springboot.report.util.HostCacheUtil;
import com.huatu.springboot.web.tools.exception.ExceptionResolver;
import com.huatu.tiku.common.bean.report.ExceptionReportMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hanchao
 * @date 2018/1/16 16:24
 */
//保证优先级最低，并且该异常未被其他用户自定义处理器处理过，才认为是报错，需要上报
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class ExceptionProducter implements ExceptionResolver {
    @Autowired
    private RabbitReporter rabbitReporter;
    @Autowired
    private MessageReportExecutor messageReportExecutor;
    @Autowired
    private Environment environment;
    @Override
    public ErrorResult resolve(Exception ex, HttpServletRequest request) {

        ServletServerHttpRequest nativeRequest = new ServletServerHttpRequest(request);
        String body = "";
        try {
            body = IOUtils.toString(nativeRequest.getBody());
        } catch(Exception e){
            e.printStackTrace();
        }

        ExceptionReportMessage message = ExceptionReportMessage.builder()
                .message(ex.getMessage())
                .exception(ex.getClass().getCanonicalName())
                .stacktrace(ExceptionUtils.getStackTrace(ex))
                .url(request.getRequestURI())
                .urlParameters(request.getQueryString())
                .method(request.getMethod())
                .body(body)
                .requestHeaders(nativeRequest.getHeaders())
                .build();

        message.setTimestamp(TimestampUtil.currentTimeStamp());
        message.setApplication(environment.getProperty("spring.application.name",""));
        message.setHost(HostCacheUtil.getHost());

        messageReportExecutor.execute(() -> {
            rabbitReporter.report(message);
        });

        //返回null,使用缺省异常
        return null;
    }

    @Override
    public boolean canResolve(Exception ex, HttpStatus httpStatus) {
        if(httpStatus != null && httpStatus.is5xxServerError() ){
            return true;
        }
        return false;
    }
}
