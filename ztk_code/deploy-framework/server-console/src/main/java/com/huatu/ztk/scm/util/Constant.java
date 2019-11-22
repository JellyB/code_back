package com.huatu.ztk.scm.util;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-16
 * Time: 上午9:39
 * To change this template use File | Settings | File Templates.
 */
public class Constant {
    public final static ModelAndView view_err = new ModelAndView("err");
    public final static ModelAndView view_login = new ModelAndView("login");
    public final static ModelAndView view_home = new ModelAndView("index");
    public final static String bin_home = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/")
            + "/WEB-INF/bin/";
    /**工程存放的基路径*/
    public static final String PROJECT_BASE_PATH="/data/projects/projects/build_tag/";
    public final static String USER_KEY = "session_user";

}
