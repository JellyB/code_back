package com.huatu.common.jwt.resolver;

import com.huatu.common.exception.UnauthorizedException;
import com.huatu.common.jwt.util.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver;

import javax.servlet.http.Cookie;

import static com.huatu.common.CommonResult.SESSION_EXPIRE;

/**
 * @author hanchao
 * @date 2018/3/26 16:51
 */
public class JWTUserMethodArgumentResolver extends ServletCookieValueMethodArgumentResolver {
    /**
     * @param beanFactory a bean factory to use for resolving  ${...}
     *                    placeholder and #{...} SpEL expressions in default values;
     *                    or {@code null} if default values are not expected to contain expressions
     */
    public JWTUserMethodArgumentResolver(ConfigurableBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JWToken.class);
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        JWToken annotation = parameter.getParameterAnnotation(JWToken.class);
        return new JWTokenNamedValueInfo(annotation);
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        Object obj = super.resolveName(name, parameter, request);
        if (obj == null) {
            return null;
        }
        String jwToken = null;
        if(obj instanceof Cookie){
            jwToken = ((Cookie) obj).getValue();
        }else if(obj instanceof String){
            jwToken = (String) obj;
        }

        if(StringUtils.isBlank(jwToken)){
            return null;
        }
        JWToken annotation = parameter.getParameterAnnotation(JWToken.class);
        try {
            return JWTUtil.parse(jwToken,annotation.verify());
        } catch(Exception e){
            throw new UnauthorizedException(SESSION_EXPIRE);
        }
    }

    @Override
    protected void handleMissingValue(String name, MethodParameter parameter) throws ServletRequestBindingException {
        throw new UnauthorizedException();
    }

    private static class JWTokenNamedValueInfo extends NamedValueInfo {

        private JWTokenNamedValueInfo(JWToken annotation) {
            super(annotation.name(), annotation.required(), null);
        }
    }
}
