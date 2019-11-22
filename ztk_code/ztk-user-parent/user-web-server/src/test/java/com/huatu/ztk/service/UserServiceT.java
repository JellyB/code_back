package com.huatu.ztk.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.BaseTest;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huangqingpeng
 * @title: UserServiceT
 * @description: TODO
 * @date 2019-11-0520:57
 */
public class UserServiceT extends BaseTest {


    @Autowired
    UserService userService;

    private static final Long base_phone = 11010000000L;

    @Test
    public void test(){

        String preName = "";
        List<UserDto> userDtoList = IntStream.rangeClosed(1, 100).boxed().map(i -> {
            String n = String.valueOf(i);
            String p = String.valueOf(i * 3);
            StringBuilder name = new StringBuilder(preName).append("0000", 0, 3 - n.length()).append(n);
            StringBuilder passwd = new StringBuilder(preName).append("0000", 0, 3 - p.length()).append(p);
            return UserDto.builder().password(passwd.toString())
                    .name(name.toString())
                    .mobile((base_phone + i) + "")
                    .build();
        }).collect(Collectors.toList());
        Map<Boolean, List<Object>> booleanListMap = userService.registerForPHP(userDtoList, "127.0.0.1", "");
        System.out.println("JsonUtil.toJson(booleanListMap) = " + JsonUtil.toJson(booleanListMap));
    }
}
