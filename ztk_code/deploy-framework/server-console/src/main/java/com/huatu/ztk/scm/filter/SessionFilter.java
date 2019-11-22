package com.huatu.ztk.scm.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.huatu.ztk.scm.util.Constant;
/**
 * session 过滤器
 * @author shaojieyue
 * @date 2013-07-17 15:30:33
 */
public class SessionFilter implements Filter {
	/**需要跳过的url*/
	private static final Set<String> urlSet = new HashSet<String>();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String skipUrl=filterConfig.getInitParameter("skipUrl");
		String[] urls =StringUtils.trimToEmpty(skipUrl).split(";");
		for(String url:urls){
			urlSet.add(url);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hr= (HttpServletRequest)request;
		String url = hr.getRequestURI();
		HttpSession session = hr.getSession();
		if(urlSet.contains(url)){//不走过滤器
			chain.doFilter(request, response);
		}else if(session==null||session.getAttribute(Constant.USER_KEY)==null){//session失效
			String contextPath=hr.getContextPath();
			((HttpServletResponse)response).sendRedirect(contextPath+"/");
		}else{
			chain.doFilter(request, response);
		}
		
		

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
