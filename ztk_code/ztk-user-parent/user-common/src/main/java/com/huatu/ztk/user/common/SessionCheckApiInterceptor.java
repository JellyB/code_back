//package com.huatu.ztk.user.common;
//
//import com.google.common.base.Strings;
//import com.google.gson.Gson;
//import com.huatu.ztk.user.bean.SessionCheck;
//import com.huatu.ztk.user.service.UserSessionService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Method;
//
///**
// * 用户session过期检测的拦截器
// * Created by lijianying on 6/16/16.
// */
//public class SessionCheckApiInterceptor extends HandlerInterceptorAdapter {
//
//    private  static final Logger logger = LoggerFactory.getLogger(SessionCheckApiInterceptor.class);
//
//    @Autowired
//    private UserSessionService userSessionService;
//
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response, Object handler) throws Exception {
//
//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        Method method = handlerMethod.getMethod();
//
//        //获取SessionCheck注解标示的方法
//        SessionCheck annotation = method.getAnnotation(SessionCheck.class);
//        if (annotation != null) {
//            //获取header中的token值
//            String token = request.getHeader("token");
//            if (Strings.isNullOrEmpty(token) || userSessionService.assertSession(token);) {//用户会话过期
//
//                logger.error("token is null or is expired : {}"+token);
//                response.getWriter().write(new Gson().toJson(UserErrors.SESSION_EXPIRE));
//                return false;
//            }
//        }
//        // 没有注解通过拦截
//        return true;
//    }
//
//}
