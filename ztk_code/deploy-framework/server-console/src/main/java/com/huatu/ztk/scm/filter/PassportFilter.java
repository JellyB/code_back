package com.huatu.ztk.scm.filter;

import com.huatu.ztk.scm.dao.UserDao;
import com.huatu.ztk.scm.util.SpringBeanUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
/**
 * 通行证过滤器，以通行证为准来校验用户是否登录
 * @author shaojieyue
 * @date 2013-08-19 18:23:31
 */
public class PassportFilter implements Filter {

	/**需要跳过的url*/
	private static final Set<String> urlSet = new HashSet<String>();
	
    private UserDao scmUserDao = SpringBeanUtils.getBean("scmUserDao",UserDao.class);
	

	public void init(FilterConfig filterConfig) throws ServletException {
		String skipUrl=filterConfig.getInitParameter("skipUrl");
		String[] urls =StringUtils.trimToEmpty(skipUrl).split(";");
		for(String url:urls){
			urlSet.add(url);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hr= (HttpServletRequest)request;
		String url = hr.getRequestURI();
		String contextPath=hr.getContextPath();
		String passport = hr.getHeader("X-SohuPassport-UserId");
		if(passport==null||"".equals(passport.trim())){
			passport= (String)hr.getSession().getAttribute("X-SohuPassport-UserId");
		}
    if("true".equals(System.getProperty("is_test"))){
        passport = "yshaojie@sohu.com";
        hr.getSession().setAttribute("X-SohuPassport-UserId",passport);
    }
    if(urlSet.contains(url)){//不走过滤器
			chain.doFilter(request, response);
		}else if("/".equals(url.trim())){//访问根目录
			((HttpServletResponse)response).sendRedirect(contextPath+"/user/login.do");
		}else if(passport==null||"".equals(passport.trim())){//没有登录
			((HttpServletResponse)response).sendRedirect(contextPath+"/user/login.do");
		}else if(!scmUserDao.userExist(passport)){//无权限
			((HttpServletResponse)response).sendRedirect(contextPath+"/user/no_permissions.do");
		}else{
			chain.doFilter(request, response);
		}
	}


	public void destroy() {
		// TODO Auto-generated method stub

	}

}
