package com.huatu.ztk.scm;

import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.dao.UserDao;
import com.huatu.ztk.scm.dto.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpStatus;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 用户登录Controller
 *
 * @author shaojieyue
 * @date 2013-07-11 14:23:59
 */
@Controller
@RequestMapping("/user/")
public class LoginController extends BaseController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Resource
    private UserDao scmUserDao;

    /**
     * 域登录操作
     *
     * @return
     */
    private static final Logger logger                   = LoggerFactory.getLogger(LoginController.class);

    /**
     * 用户登录操作
     *
     * @return
     */
    @RequestMapping(value = "login.do")
    public String login( HttpServletRequest request, HttpServletResponse response) {

		if(request.getMethod().equals("POST")){
			String userName = StringUtils.trimToEmpty(request.getParameter("username"));
			String pwd = StringUtils.trimToEmpty(request.getParameter("pwd"));

			if(userName==null||"".equals(userName.trim())){
				return "login";
			}
			// passport 验证
			User user=scmUserDao.get(userName);

			System.out.println(user);
			//boolean exist = scmUserDao.userExist(userName);
			if(user!=null){
				if (pwd.equals(user.getPwd())) {
					logger.info("-->" + userName + " login at " + DateFormatUtils.format(new Date(), DATE_TIME_PATTERN));
					request.getSession().setAttribute("X-SohuPassport-UserId", userName);
					return "redirect:index.do";
					//	return "no_permissions";
				} else {
					logger.info("mima cuo");
					return "no_permissions";
				}
			}else {
				return "no_permissions";
			}
		}else {
			return "login";
		}


    }
    
    @RequestMapping("toLogin.do")
    public String toLogin(){
    	return "login";
    }

    @RequestMapping(value = "no_permissions.do")
    public String noPermissions(){
    	String userName = this.getCurrentPassport();
    	if(userName==null||"".equals(userName.trim())){
    		return "login";
    	}
    	return "no_permissions";
    }
    
    @RequestMapping(value = "index.do")
    public String index(){
    	return "index";
    }
    
    @RequestMapping("userExist.do")
    @ResponseBody
    public String userExist(@RequestParam("userCode") String userCode){
    	boolean exist = scmUserDao.userExist(userCode);
    	JSONObject json = new JSONObject();
    	json.put("result", exist);
    	return json.toString();
    }
    
    @RequestMapping("logout.do")
    public String logout(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		request.getSession().removeAttribute("X-SohuPassport-UserId");
		logger.info("-->" + this.getCurrentPassport() + " logout at " + DateFormatUtils.format(new Date(), DATE_TIME_PATTERN));
        return "redirect:toLogin.do";
    }
    
    @RequestMapping("addUser.do")
    @ResponseBody
    public String addUser(@RequestParam("passport")String passport,@RequestParam("userName")String userName){
    	boolean b  = false;
    	String msg = "添加成功";
    	try{
    		 b = scmUserDao.insert(passport, this.getCurrentPassport(),userName);
    		 if(!b){
    			 msg="添加失败";
    		 }
    	}catch (DuplicateKeyException e) {
    		msg = "用户已存在";
    		b= false;
		}
    	JSONObject json = new JSONObject();
    	if(b){
    		json.put("type", "success");
    	}else{
    		json.put("type", "fail");
    	}
    	json.put("msg", msg);
    	return json.toString();
    }
    
    @RequestMapping("deleteUser.do")
    @ResponseBody
    public String deleteUser(@RequestParam("passport")String passport){
    	boolean b  = scmUserDao.delete(passport);
    	JSONObject json = new JSONObject();
    	if(b){
    		json.put("type", "success");
    		json.put("msg", "删除成功");
    	}else{
    		json.put("type", "fail");
    		json.put("msg", "删除失败");
    	}
    	return json.toString();
    }
    
    @RequestMapping("queryAllUser.do")
    @ResponseBody
    public String queryAllUser(){
    	List<User> lists = scmUserDao.queryAllUser();
    	JSONArray arr = JSONArray.fromObject(lists);
    	return arr.toString();
    }

}