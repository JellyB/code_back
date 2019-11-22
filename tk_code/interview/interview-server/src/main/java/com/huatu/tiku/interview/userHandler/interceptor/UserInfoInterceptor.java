package com.huatu.tiku.interview.userHandler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by junli on 2018/4/11.
 */
@Component
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long adminId = devModel(request);
        try {
            UserInfoHolder.set(adminId);
        } finally {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserInfoHolder.clear();
    }

    private static long devModel(HttpServletRequest request) {
        String adminId = request.getHeader("admin_id");
        log.info("admin_id = {}",adminId);
        if (StringUtils.isEmpty(adminId)) {
            return 1;
        }
        return Long.valueOf(adminId);
    }
}
