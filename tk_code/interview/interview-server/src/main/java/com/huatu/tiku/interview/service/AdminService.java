package com.huatu.tiku.interview.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @Author jbzm
 * @Date Create on 2018/1/23 10:28
 */
public interface AdminService  extends UserDetailsService {
    UserDetails loadByUsernameAndPassword(String username, String password);
}
