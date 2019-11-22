package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author jbzm
 * @date Create on 2018/3/12 12:01
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
