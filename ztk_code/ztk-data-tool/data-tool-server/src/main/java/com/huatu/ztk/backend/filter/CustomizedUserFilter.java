package com.huatu.ztk.backend.filter;

import com.huatu.ztk.commons.JsonUtil;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-12  18:25 .
 */

public class CustomizedUserFilter extends UserFilter {
    private static final Logger log = LoggerFactory.getLogger(CustomizedUserFilter.class);
    /**
     * 检查用户是否登录，若登录，返回true，若未登录，返回false，并返回状态401
     * @param servletRequest
     * @param servletResponse
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue) {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        final HttpSession session = request.getSession();
        if (isLoginRequest(request, response)|| session.getAttribute("user") != null) {//表示已经登录成功
            return true;
        } else {//未登录
            if (session == null || session.getAttribute("user") == null) {
                response.setStatus(401);
            }
            return false;
        }
    }

    /**
     * 直接返回false，表示跳转到登录页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception  {
        String loginUrl = getLoginUrl();
        log.info("拒绝后，跳转的页面={}", JsonUtil.toJson(loginUrl));
       return false;
    }
}
