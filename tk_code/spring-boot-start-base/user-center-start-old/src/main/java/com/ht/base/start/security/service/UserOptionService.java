package com.ht.base.start.security.service;

import com.ht.base.dto.ResponseData;
import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengyi
 * @date 2019-02-27 16:39
 **/
public interface UserOptionService {
    /**
     * login
     *
     * @return token
     */
    @RequestLine("GET /uc/user/{id}")
    ResponseData getUserInfo(@HeaderMap Map<String, Object> map, @Param("id") Long id);

    @RequestLine("GET /uc/user/findByMenus?menuIds={menuIds}")
    ResponseData getUserByMenus(@HeaderMap Map<String, Object> map, @Param("menuIds") List<Long> menuIds);

    @RequestLine("GET /uc/user/findByRoles?roleIds={roleIds}")
    ResponseData getUserByRoles(@HeaderMap Map<String, Object> map, @Param("roleIds") List<Long> roleIds);
}
