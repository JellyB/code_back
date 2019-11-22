package com.huatu.keycloak.web.controller;

import com.huatu.common.bean.page.Pager;
import com.huatu.keycloak.bean.UserInfo;
import com.huatu.keycloak.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/10/17 15:40
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public List<UserInfo> search(@RequestParam(required = false,defaultValue = "") String search,
                               Pager pager){
        return userService.search(search,pager);
    }

    @GetMapping("/{id}")
    public UserInfo get(@PathVariable String id){
        return userService.get(id);
    }




}
