package com.huatu.ztk.scm.base;

import com.huatu.ztk.scm.dao.UserDao;
import com.huatu.ztk.scm.dto.User;
import com.huatu.ztk.scm.util.SpringBeanUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {
	
        protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected HttpServletRequest getRequest() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return attr.getRequest();
	}
	
	protected String getCurrentPassport(){
        if("true".equals(System.getProperty("is_test"))){
            return "yshaojie@sohu.com";
        }
		String passport =  this.getRequest().getHeader("X-SohuPassport-UserId");
        if(passport==null||"".equals(passport.trim())){
            passport= (String)this.getRequest().getSession().getAttribute("X-SohuPassport-UserId");
        }
        return passport;
	}

    protected String getPassportInc(String passport){

        if("true".equals(System.getProperty("is_test"))){
            return "sarowliu@sohu-inc.com";
        }

        UserDao userDao = SpringBeanUtils.getBean("scmUserDao", UserDao.class);
        User user = userDao.get(passport);
        if(user!=null){
            return user.getPassportInc();
        }
        return "";
    }

	protected String getUserName(){
		String passport =  getCurrentPassport();
		UserDao userDao = SpringBeanUtils.getBean("scmUserDao", UserDao.class);
		User user = userDao.get(passport);
		if(user!=null){
			return user.getUserName();
		}
		return "";
	}

    protected User getUser(){
        String passport =  getCurrentPassport();
        UserDao userDao = SpringBeanUtils.getBean("scmUserDao", UserDao.class);
        User user = userDao.get(passport);
        return user;
    }
	
	protected void setErrorMsg(String errorMsg){
		setMsg("fail",errorMsg);
	}
	
	protected void setSuccessMsg(String successMsg){
		setMsg("success", successMsg);
	}
	
	private void setMsg(String type,String msg){
		JSONObject json = new JSONObject();
		try {
			json.put("type",type);
			json.put("msg",msg );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.getRequest().setAttribute("returnMsg", json.toString());
	}
}
