package com.huatu.ztk.question.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域
 * Created by x6 on 2018/6/14.
 */
public class AccessFilter extends HttpServlet implements Filter {
    private static final long serialVersionUID = 1L;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;

        HttpServletRequest reqs = (HttpServletRequest) req;

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "content-type,terminal,token");
        response.setHeader("Access-Control-Expose-Headers", "*");

        if (reqs.getMethod().equals("OPTIONS")) {
            response.setStatus(200);
            return;
        }
        chain.doFilter(req, res);
    }
}
