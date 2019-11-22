package com.huatu.ztk.user.controller;

import com.huatu.ztk.user.bean.UserSearchRequest;
import com.huatu.ztk.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhengyi
 * @date 11/14/18 3:24 PM
 **/
@RestController
@Slf4j
@Deprecated
@RequestMapping("/php")
public class PHPCheckDataController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Object search(UserSearchRequest userSearchRequest) {
        return userService.searchUserListForRegFromAndTime(userSearchRequest);
    }
}