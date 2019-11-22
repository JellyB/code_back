package com.huatu.ztk.backend.filter;

import com.huatu.ztk.backend.system.bean.*;
import com.huatu.ztk.backend.system.dao.*;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-13  19:13 .
 */
public class CustomizedRoleFilter extends RolesAuthorizationFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomizedUserFilter.class);
    @Autowired
    private ActionManageDao actionManageDao;
    @Autowired
    private OperateDao operateDao;

    /**
     * 先判断用户是否登录，若没有登录跳转到登录页面，再判断用户请求的地址是否合法，合法直接进行跳转，不合法交于onAccessDenied方法进行处理
     */
    @Override
    @SuppressWarnings({"unchecked"})
    public boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue) throws IOException {
        long stime = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        final HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        final String url = request.getContextPath()+request.getServletPath()+request.getPathInfo();//用户访问地址
        String lastUrl = getUrl(url);
        List<OperateMessage> operates = operateDao.findOperateNormalByUrl(lastUrl);
        List<Action> actions = new ArrayList<>();
        if (session == null || session.getAttribute("user") == null) {//判断用户是否登录
            response.setStatus(401);
        }else{
            actions = actionManageDao.findActionByUid((int) user.getId());
        }
        boolean isAllowed = false;
        if(operates!=null&&operates.size()>0){
            for(Action action:actions){//判断用户请求操作是否绑定在菜单（用户有权限）之下
                if(action.getId()==operates.get(0).getActionId()){
                    isAllowed = true;
                    break;
                }
            }
        }
        if(isAllowed==false){
            log.info("role过滤器拒绝访问的url={}", JsonUtil.toJson(url));
        }
        log.info("access utime={}", System.currentTimeMillis() - stime);
        return isAllowed;
    }

    /**
     * 用户请求不合法时，返回用户拒绝页面
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        log.info("跳转到拒绝页面={}", JsonUtil.toJson(getUnauthorizedUrl()));
        WebUtils.issueRedirect(servletRequest, servletResponse, getUnauthorizedUrl());//跳转拒绝页面
        return false;
    }


    //提取路径，若为.html格式，路径最后网址，否则，返回整个路径
    public String getUrl(String str){
        String[] strs = str.split("/");
        if(!str.contains(".html")){
            if(isNumeric(strs[strs.length-1])){//若路径末尾为数字，先去除
                str = str.replace(strs[strs.length-1],"");
            }
            return str;
        }else{
            if(strs.length!=0){
                return strs[strs.length-1];
            }else{
                return null;
            }
        }
    }

    //判断字符串是否为数字
    public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
}
