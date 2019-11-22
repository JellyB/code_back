package com.huatu.tiku.interview.service.impl;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.InterviewErrors;
import com.huatu.tiku.interview.entity.po.Admin;
import com.huatu.tiku.interview.repository.AdminRepository;
import com.huatu.tiku.interview.service.AdminService;
import com.huatu.tiku.interview.spring.conf.web.AdminInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Author jbzm
 * @Date Create on 2018/1/23 10:28
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadByUsernameAndPassword(String username, String password) {
        //查询时候已判断用户状态，无需再次递交下层判断
       Admin user = adminRepository.findByUsername(username);
       if (user == null) {
           throw new BizException(InterviewErrors.USER_NOT_EXIST);
       }
       if (!password.equals(user.getPassword())) {
           throw new BizException(InterviewErrors.PASSWORD_ERROR);
       }
        return add(user);
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }

    private AdminInfo add(Admin user) {
        return AdminInfo.builder().id(user.getId()).username(user.getUsername()).password(user.getPassword()).createTime(user.getGmtCreate()).status(user.getStatus()).build();
    }
}
