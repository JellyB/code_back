package com.huatu.tiku.interview.spring.conf.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author zhouwei
 * @Description: 登录校验
 * @create 2017-12-14 上午10:26
 **/
//@Slf4j
//@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o) throws Exception {
      //  log.info("---------------preHandle-------------------");

                 /*可以通过的ip，*代表所有*/
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            /* 支持证书 */
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Token");
            response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
        HttpSession session = httpServletRequest.getSession();

//        EssayAdmin essayAdmin = (EssayAdmin)httpServletRequest.getSession().getAttribute("user");
//        if(essayAdmin==null){
//            log.info("---------------未登录-------------------");
//            final ErrorResult errorResult = ErrorResult.create(2000003, "未登录");
//            throw new BizException(errorResult);
//        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
