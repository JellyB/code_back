package com.huatu.ztk.pc.controller;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.dubbo.UserDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by shaojieyue
 * Created time 2016-09-08 17:39
 */

@Controller
@RequestMapping(value = "/user/")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDubboService userDubboService;

    /**
     * 获取登录页面
     *
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login() {
        return "wechat/login";
    }

    /**
     * 执行登录操作
     *
     * @param account
     * @param password
     * @param response
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object dologin(String account, String password,
                          HttpServletRequest httpServletRequest,
                          HttpServletResponse response) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        final UserSession session = userDubboService.login(account, password, TerminalType.WEI_XIN,"");
        Cookie cookie = getSessionCookie(session.getToken());
        response.addCookie(cookie);
        return SuccessMessage.create("账户有效");
    }

    private Cookie getSessionCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setMaxAge(7776000);
        return cookie;
    }

    /**
     * 获取注册页面
     *
     * @return
     */
    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register(HttpServletRequest request,HttpServletResponse response) {
        return "wechat/register";
    }

    /**
     * 执行注册操作
     *
     * @return
     */
    @RequestMapping(value = "register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object doregister(@RequestParam String phone,
                             @RequestParam String verification,
                             @RequestParam String password,
                             HttpServletRequest httpServletRequest,
                             HttpServletResponse response) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        final UserSession session = userDubboService.register(phone, verification, password, TerminalType.WEI_XIN,"");
        Cookie cookie = getSessionCookie(session.getToken());
        response.addCookie(cookie);
        return SuccessMessage.create("注册成功");
    }

    /**
     * 注销
     *
     * @return
     */
    @RequestMapping(value = "logout", produces = MediaType.TEXT_HTML_VALUE)
    public String logout(@RequestToken String token) throws BizException {
        userDubboService.logout(token);
        return "redirect:/user/login";
    }

}
