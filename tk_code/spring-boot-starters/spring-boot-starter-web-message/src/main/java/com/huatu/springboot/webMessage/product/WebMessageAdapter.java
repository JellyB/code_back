package com.huatu.springboot.webMessage.product;

import com.huatu.common.utils.date.TimestampUtil;
import com.huatu.springboot.webMessage.util.HostCacheUtil;
import com.huatu.springboot.webMessage.annotation.WebReport;
import com.huatu.springboot.webMessage.core.RabbitMqReportQueueEnum;
import com.huatu.springboot.webMessage.support.WebMessageReportExecutor;
import com.huatu.springboot.webMessage.support.impl.RabbitMqReport;
import com.huatu.tiku.common.bean.report.WebReportMessage;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.tiku.springboot.users.support.UserSessionHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 定义全局的过滤器
 * Created by junli on 2018/4/10.
 */
@Slf4j
public class WebMessageAdapter implements HandlerInterceptor {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebMessageReportExecutor webMessageReportExecutor;

    @Autowired
    private RabbitMqReport rabbitReport;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        WebReport webReport = getWebReport(handler);
        if (null != webReport) {
            try {
                ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
                ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
                String body = IOUtils.toString(httpRequest.getBody());
                //处理用户信息部分
                UserSession userSession = UserSessionHolder.get();
                if (null == userSession) {
                    String token = request.getHeader(Token.DEFAULT_NAME);
                    userSession = userSessionService.getUserSession(token);
                }

                //处理异常信息部分
                String stacktrace = "";
                Object exception = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
                if (exception != null && exception instanceof Exception) {
                    stacktrace = ExceptionUtils.getStackTrace((Exception) exception);
                }
                WebReportMessage webReportMessage = WebReportMessage.builder()
                        .name(webReport.value())
                        .url(request.getRequestURI())
                        .urlParameters(request.getQueryString())
                        .method(request.getMethod())
                        .body(body)
                        .status(response.getStatus())
                        .requestHeaders(httpRequest.getHeaders())
                        .responseHeaders(httpResponse.getHeaders())
                        .status(response.getStatus())
                        .stacktrace(stacktrace)
                        .userSession(userSession)
                        .build();
                webReportMessage.setTimestamp(TimestampUtil.currentTimeStamp());
                webReportMessage.setApplication(applicationContext.getEnvironment().getProperty("spring.application.name", ""));
                webReportMessage.setHost(HostCacheUtil.getHost());
                webReportMessage.setReturnValue(ResponseResultHolder.get());

                //上报
                RabbitMqReportQueueEnum[] queueNameEnum = webReport.queueName();
                webMessageReportExecutor.execute(this.new ReportTask(queueNameEnum, webReportMessage, webReport.extraHandler()));

            } finally {
                ResponseResultHolder.clear();
            }
        }

    }

    protected class ReportTask implements Runnable {
        private RabbitMqReportQueueEnum[] queue;//队列名称
        private WebReportMessage message;
        private Class<?> handlerClass;

        public ReportTask(RabbitMqReportQueueEnum[] queue, WebReportMessage message, Class<?> handlerClass) {
            this.queue = queue;
            this.message = message;
            this.handlerClass = handlerClass;
        }

        @Override
        public void run() {
            if (handlerClass != null && ExtraDataHandler.class.isAssignableFrom(handlerClass)) {
                try {
                    if (applicationContext.getBean(handlerClass) == null) {
                        log.error("cant find extra data handler for class {}", handlerClass);
                    } else {
                        ExtraDataHandler handler = (ExtraDataHandler) applicationContext.getBean(handlerClass);
                        //此处message 已经完成了数据自主组装
                        Object extra = handler.extra(message);
                        message.setExtraData(extra);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //extra方法如果将消息设置未null，则不发送此消息了
            if (message != null) {
                for (RabbitMqReportQueueEnum queueEnum : queue) {
                    //当前上报的数据是否只上报 自定义数据,减少IO 流
                    boolean simpleData = queueEnum.isSimpleData() && message instanceof WebReportMessage;
                    rabbitReport.report(queueEnum.getQueueName(), simpleData ? message.getExtraData() : message);
                }
            }
        }
    }

    public static WebReport getWebReport(Object handler) {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            WebReport annotation = ((HandlerMethod) handler).getMethodAnnotation(WebReport.class);
            return annotation;
        }
        return null;
    }
}
