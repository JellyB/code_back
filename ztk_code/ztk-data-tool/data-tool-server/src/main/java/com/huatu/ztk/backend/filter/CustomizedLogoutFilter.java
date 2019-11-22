package com.huatu.ztk.backend.filter;

import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.apache.shiro.web.util.WebUtils.issueRedirect;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-16  15:32 .
 */
public class CustomizedLogoutFilter extends UserFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomizedLogoutFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue) {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        final HttpSession session = request.getSession();
        //try/catch added for SHIRO-298:
        try {
            session.invalidate();//注销用户,
            response.setStatus(500);
            return false;
        } catch (SessionException ise) {
            log.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
            return true;
        }
    }


}
