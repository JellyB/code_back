package com.huatu.tiku.interview.util.filter;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 16:29
 */
//@WebFilter(filterName = "wx",urlPatterns="/wx/api/oca/*")
public class WXFilter implements Filter {
//TODO 不确定使用这个
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
