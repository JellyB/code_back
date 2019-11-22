package com.huatu.ztk.commons.spring.resolver;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Created by shaojieyue
 * Created time 2016-09-20 16:38
 */
public class CommonHandlerExceptionResolver extends SimpleMappingExceptionResolver {
    /**
     * Try to resolve the given exception that got thrown during handler execution,
     * returning a {@link ModelAndView} that represents a specific error page if appropriate.
     * <p>The returned {@code ModelAndView} may be {@linkplain ModelAndView#isEmpty() empty}
     * to indicate that the exception has been resolved successfully but that no view
     * should be rendered, for instance by setting a status code.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the executed handler, or {@code null} if none chosen at the
     *                 time of the exception (for example, if multipart resolution failed)
     * @param ex       the exception that got thrown during handler execution
     * @return a corresponding {@code ModelAndView} to forward to, or {@code null}
     * for default processing
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (ex instanceof BizException) {
            final ErrorResult result = ((BizException) ex).getErrorResult();
            //用户session过期||其他设备登录
            if (result.getCode() == CommonErrors.SESSION_EXPIRE.getCode()
                    || result.getCode() == CommonErrors.LOGIN_ON_OTHER_DEVICE.getCode()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }


        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //Accept
        if (handler instanceof HandlerMethod) {
            String requestContentType = getRequestContentType(request);
            if (requestContentType != null) {//优先处理request 含有content type
                requestContentType = requestContentType.toLowerCase();
                if (requestContentType.startsWith(MediaType.TEXT_HTML_VALUE)) {//html
                    return drawHtml(request,response,ex);
                }else if (requestContentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {//json
                    return drawJson(response,ex);
                }
            }

            //优先检查 method的RequestMapping
            final RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
            if (requestMapping != null &&  requestMapping.produces()!= null && requestMapping.produces().length>0) {//处理RequestMapping 含有指定输出类型
                return drawView(request, response, ex,requestMapping.produces()[0]);
            }

            //其次检查controller的RequestMapping
            final RequestMapping classRequestMapping = handlerMethod.getMethod().getDeclaringClass().getAnnotation(RequestMapping.class);
            if (classRequestMapping != null &&  classRequestMapping.produces()!= null && classRequestMapping.produces().length>0) {//处理RequestMapping 含有指定输出类型
                return drawView(request, response, ex,classRequestMapping.produces()[0]);
            }

            final ResponseBody responseBody = handlerMethod.getMethodAnnotation(ResponseBody.class);
            if (responseBody != null) {//存在注解ResponseBody,则认为返回json
                return drawJson(response,ex);
            }else {
                return drawHtml(request,response,ex);
            }
        }

        return null;
    }

    private ModelAndView drawView(HttpServletRequest request, HttpServletResponse response, Exception ex,String contentType) {
        contentType = contentType.toLowerCase();
        if (contentType.startsWith(MediaType.TEXT_HTML_VALUE)) {
            return drawHtml(request,response,ex);
        }else {
            return drawJson(response,ex);
        }
    }

    /**
     * 获取请求要求的返回类型
     * @param request
     * @return
     */
    private String getRequestContentType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            contentType = request.getHeader("Accept");
        }

        return contentType;
    }

    private ModelAndView drawHtml(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        // Expose ModelAndView for chosen error view.
//        String viewName = determineViewName(ex, request);
        // 如果不是异步请求
        // Apply HTTP status code for error views, if specified.
        // Only apply it if we're processing a top-level request.
//        Integer statusCode = determineStatusCode(request, viewName);
//        if (statusCode != null) {
//            applyStatusCodeIfPossible(request, response, statusCode);
//        }
        final ModelAndView modelAndView = getModelAndView(ex);
        return modelAndView;
    }

    private ModelAndView getModelAndView(Exception exception) {
        ErrorResult errorResult = getErrorResult(exception);
        return new ModelAndView("redirect:http://ns.huatu.com/pc/error/5xx?code="+errorResult.getCode()+"&message="+errorResult.getMessage());
    }

    /**
     * 处理json返回结果
     * @param response
     * @param ex
     * @return
     */
    private ModelAndView drawJson(HttpServletResponse response,Exception ex) {
        final MappingJackson2JsonView jackson2JsonView = new MappingJackson2JsonView();
        jackson2JsonView.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ModelAndView modelAndView = new ModelAndView(jackson2JsonView);
        ErrorResult errorResult = getErrorResult(ex);
        modelAndView.addObject("code",errorResult.getCode());
        if (errorResult.getData() != null) {
            modelAndView.addObject("data",errorResult.getData());
        }
        if (errorResult.getMessage() != null) {
            modelAndView.addObject("message",errorResult.getMessage());
        }
        return modelAndView;
    }

    private ErrorResult getErrorResult(Exception ex){
        if (ex instanceof BizException) {//业务异常，此处不打印错误日志
            final BizException bizException = (BizException) ex;
            final String customMessage = bizException.getCustomMessage();
            if (customMessage != null) {//自定义信息
                return ErrorResult.create(bizException.getErrorResult().getCode(),customMessage);
            }
            return bizException.getErrorResult();
        }else if(ex instanceof ServletException){//spring exception
            //返回非法参数错误，此处采用自定义的描述信息
            return ErrorResult.create(CommonErrors.INVALID_ARGUMENTS.getCode(),ex.getMessage());
        }else if(ex instanceof HttpMessageConversionException){
            //返回非法参数错误，此处采用自定义的描述信息
            return ErrorResult.create(CommonErrors.INVALID_ARGUMENTS.getCode(),ex.getMessage());
        }
        logger.error("ex",ex);
        return CommonErrors.SERVICE_INTERNAL_ERROR;
    }
}
