package com.huatu.tiku.interview.spring.conf.web;

import com.huatu.tiku.interview.service.AdminService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author jbzm
 * @date Create on 2018/3/13 13:02
 */
public class CustomDaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private AdminService adminService;

    public CustomDaoAuthenticationProvider(AdminService adminService) {
        this.adminService = adminService;
        this.setHideUserNotFoundExceptions(false);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        //使用自定义的用户加载流程，此处无需继续验证
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        return adminService.loadByUsernameAndPassword(username, authentication.getCredentials().toString());
    }
}

