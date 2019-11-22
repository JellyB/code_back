package com.arj.monitor.aop.session;

import com.arj.monitor.common.CommonConfig;
import com.arj.monitor.common.CommonResult;
import com.arj.monitor.entity.UserSession;
import com.arj.monitor.exception.BizException;
import com.arj.monitor.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;

/**
 * @author zhouwei
 * @Description: token解析
 * @create 2018-10-15 上午12:04
 **/
@Slf4j
public class TokenMethodArgumentResolver extends RequestHeaderMethodArgumentResolver {
    private TokenService tokenService;

    /**
     * @param beanFactory a bean factory to use for resolving  ${...}
     *                    placeholder and #{...} SpEL expressions in default values;
     *                    or {@code null} if default values are not expected to have expressions
     */
    public TokenMethodArgumentResolver(ConfigurableBeanFactory beanFactory, TokenService tokenService) {
        super(beanFactory);
        this.tokenService = tokenService;
    }
    /**
     *  判断参数是否支持
     *  加了Token注解，参数是否是UserSession类型
     *
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.hasParameterAnnotation(Token.class) && UserSession.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        Token annotation = parameter.getParameterAnnotation(Token.class);
        return new TokenNamedValueInfo(annotation);
    }

    /**
     * 参数装配
     *
     * @param name
     * @param parameter
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
//        String token = (String) super.resolveName(name, parameter, request);
//        Token config = parameter.getParameterAnnotation(Token.class);
//
//        //从内存中获取，如果没有再去db中查找，减少访问次数
//        UserSession userSession = UserSessionHolder.userSessionCache.getIfPresent(token);
//
//        if(userSession == null){
//            userSession = tokenService.getUserSessionByToken(token);
//        }
//        if (config.check()) {
//            tokenService.assertSession(userSession);
//        }
//        //重新续约
//        userSession.setExpireTime(System.currentTimeMillis() + (CommonConfig.USER_TOKEN_TIME_OUT * 1000));
//        tokenService.updateSession(userSession);
//        UserSessionHolder.userSessionCache.put(token,userSession);
//        return userSession;

        String token = (String) super.resolveName(name, parameter, request);
        Token config = parameter.getParameterAnnotation(Token.class);
        if (token==null  &&  config.required()) {
            throw new BizException(CommonResult.SESSION_EXPIRE);
            // return null;
        }else if(token==null  &&  !config.required()) {
            return null;
        }
        UserSession userSession = tokenService.getUserSessionFromRedis(token);
        if(userSession == null){
            throw new BizException(CommonResult.SESSION_EXPIRE);
        }
        if (config.required()) {
            tokenService.assertSession(userSession);
        }else if(userSession != null){
            //重新续约
            userSession.setExpireTime(System.currentTimeMillis() + (CommonConfig.USER_TOKEN_TIME_OUT * 1000));
            tokenService.updateSessionExpireTime(token);
        }
        return userSession;
    }



    private static class TokenNamedValueInfo extends NamedValueInfo {

        private TokenNamedValueInfo(Token annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }

}
