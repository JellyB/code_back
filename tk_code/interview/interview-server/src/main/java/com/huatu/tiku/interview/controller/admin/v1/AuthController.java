package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.common.BaseResult;
import com.huatu.common.LoginResult;
import com.huatu.tiku.interview.constant.WebParamConsts;
import com.huatu.tiku.interview.spring.conf.web.AdminInfo;
import com.huatu.tiku.interview.util.LogPrint;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 后台用户登录
 * @date 2017/12/27 9:56
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    /**
     * 适配session过期等的检查
     *
     * @return
     */
    @LogPrint
    @RequestMapping("/tologin")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResult tologin() {
        return BaseResult.create(LoginResult.UNAUTHORIZED.getCode(), LoginResult.UNAUTHORIZED.getMessage(), "");
    }

    @LogPrint
    @RequestMapping("/denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseResult denied() {
        return BaseResult.create(LoginResult.FORBIDDEN.getCode(), LoginResult.FORBIDDEN.getMessage(), "");
    }

    /**
     * 获取账户信息的接口
     *
     * @return
     */
    @LogPrint
    @RequestMapping("/get")
    public Object get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof AdminInfo) {
            return BaseResult.create(20000, "", principal);
        } else {
            return BaseResult.create(LoginResult.UNAUTHORIZED.getCode(), LoginResult.UNAUTHORIZED.getMessage(), "");
        }
    }

    @LogPrint
    @RequestMapping(value = "/success", params = "login")
    public BaseResult loginSuccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        return BaseResult.create(20000, "", principal);
    }

    @LogPrint
    @RequestMapping(value = "/success", params = "logout")
    public BaseResult logoutSuccess() {
        return BaseResult.create(20000, "操作成功", "");
    }

    /**
     * 登陆失败返回信息
     *
     * @param request
     * @return
     */
    @LogPrint
    @RequestMapping("/fail")
    public BaseResult fail(HttpServletRequest request) {
        AuthenticationException authenticationException = (AuthenticationException) request.getAttribute(WebParamConsts.SPRING_SECURITY_EX);
      return BaseResult.create(1104,"用户名或密码错误","");
    }


}
