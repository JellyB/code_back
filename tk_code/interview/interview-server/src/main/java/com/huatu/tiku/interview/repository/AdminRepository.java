package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author jbzm
 * @Date Create on 2018/1/23 10:35
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {
    /**
     * 验证用户登录
     * @param username
     * @param password
     * @return
     */
    Admin findByUsernameAndPassword(String username, String password);

    Admin findByUsername(String username);
}
