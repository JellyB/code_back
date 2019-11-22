package com.huatu.tiku.position.base.config;

import com.huatu.tiku.position.base.exception.NoAuthException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**自定义拦截器
 * @author wangjian
 **/
@Component
public class WeiXinInterceptor implements HandlerInterceptor{


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String openId = httpServletRequest.getHeader("openId");
        if(StringUtils.isNotBlank(openId)){
            return true;
        }
        throw new NoAuthException("请授权");
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
