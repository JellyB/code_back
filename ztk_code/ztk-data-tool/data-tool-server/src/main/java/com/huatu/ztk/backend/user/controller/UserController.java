package com.huatu.ztk.backend.user.controller;

import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.common.errors.UserErrors;
import com.huatu.ztk.backend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by shaojieyue
 * Created time 2016-11-04 16:04
 */

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "login", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object login(@RequestBody User param, HttpServletRequest request){
        User user = userService.login(param.getAccount(),param.getPassword(),request);
        if (user != null) {
            final HttpSession session = request.getSession(true);
            session.setAttribute("user",user);
            return user;
        }else {
            return UserErrors.LOGIN_FAIL;
        }
    }

    /**
     * 用户退出登录
     * @param request
     */
    @RequestMapping(value = "logout", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void logout(HttpServletRequest request){
        request.getSession().invalidate();
    }
}
