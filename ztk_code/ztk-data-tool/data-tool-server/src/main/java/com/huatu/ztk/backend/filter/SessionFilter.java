package com.huatu.ztk.backend.filter;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by shaojieyue
 * Created time 2016-12-01 15:31
 */
public class SessionFilter  implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(SessionFilter.class);
    public static final String LOGIN_URL="/users/login";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        final String requestURI = request.getRequestURI();
        if (requestURI.equals(LOGIN_URL)
                || requestURI.equals("/") || requestURI.startsWith("/app/") || requestURI.startsWith("/vendor/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            System.out.println(requestURI);
            return;
        }


        final HttpSession session = request.getSession();
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(401);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
