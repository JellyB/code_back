package com.huatu.one.biz.controller.api.v1;

import com.huatu.one.biz.dto.UserRegisterDto;
import com.huatu.one.biz.model.User;
import com.huatu.one.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户
 *
 * @author geek-s
 * @date 2019-08-26
 */
@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 检测用户状态
     *
     * @param openid 微信ID
     * @return 状态
     */
    @GetMapping("check")
    public Object check(@RequestHeader String openid,@RequestHeader String version) {
        return userService.check(openid,version);
    }

    /**
     * 获取用户微信ID
     *
     * @param code
     * @return
     */
    @GetMapping(value = "/getOpenid")
    public Object getOpenid(@RequestParam("code") String code) {
        return userService.getOpenid(code);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDto 用户注册参数
     * @return 操作结果
     */
    @PostMapping(value = "/register")
    public void register(@RequestHeader String openid, @RequestBody UserRegisterDto userRegisterDto) {
        User user = new User();
        user.setMobile(userRegisterDto.getMobile());
        user.setUsername(userRegisterDto.getUsername());
        user.setOpenid(openid);

        userService.register(user);
    }

    /**
     * 获取用户权限
     *
     * @param openid 微信ID
     * @return 权限IDs
     */
    @GetMapping("menus")
    public Object menus(@RequestHeader String openid) {
        return userService.getMenus(openid);
    }
}
