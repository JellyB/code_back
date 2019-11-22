package com.huatu.ztk.commons.spring.resolver;

import com.huatu.ztk.commons.spring.annotation.RequestToken;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * request token 解析
 * 该解析器用来解析token
 * token来源header和cookie,以header优先
 * Created by shaojieyue
 * Created time 2016-09-20 14:05
 */
public class RequestTokenMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    /**
     * Create the {@link NamedValueInfo} object for the given method parameter. Implementations typically
     * retrieve the method annotation by means of {@link MethodParameter#getParameterAnnotation(Class)}.
     *
     * @param parameter the method parameter
     * @return the named value information
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestToken annotation = parameter.getParameterAnnotation(RequestToken.class);
        return new RequestTokenMethodArgumentResolver.RequestTokenNamedValueInfo(annotation);
    }

    /**
     * Resolves the given parameter type and value name into an argument value.
     *
     * @param name      the name of the value being resolved
     * @param parameter the method parameter to resolve to an argument value
     * @param request   the current request
     * @return the resolved argument. May be {@code null}
     * @throws Exception in case of errors
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        String[] headerValues = request.getHeaderValues(name);
        if (headerValues != null) {//优先获取head里面的token
            return (headerValues.length == 1 ? headerValues[0] : headerValues);
        }

        //header里面没有token,则从cookie里面获取
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        Cookie cookieValue = WebUtils.getCookie(servletRequest, name);
        if (Cookie.class.isAssignableFrom(parameter.getParameterType())) {
            return cookieValue;
        } else if (cookieValue != null) {
            return this.urlPathHelper.decodeRequestString(servletRequest, cookieValue.getValue());
        } else {
            return null;
        }
    }

    /**
     * Invoked when a named value is required, but {@link #resolveName(String, MethodParameter, NativeWebRequest)}
     * returned {@code null} and there is no default value. Subclasses typically throw an exception in this case.
     *
     * @param name      the name for the value
     * @param parameter the method parameter
     */
    @Override
    protected void handleMissingValue(String name, MethodParameter parameter) throws ServletException {
        throw new ServletRequestBindingException("Missing request header '" + name +
                "' for method parameter of type " + parameter.getParameterType().getSimpleName());
    }

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by this resolver.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.hasParameterAnnotation(RequestToken.class) &&
                !Map.class.isAssignableFrom(parameter.getParameterType()));
    }

    private static class RequestTokenNamedValueInfo extends NamedValueInfo {

        private RequestTokenNamedValueInfo(RequestToken annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }
}
