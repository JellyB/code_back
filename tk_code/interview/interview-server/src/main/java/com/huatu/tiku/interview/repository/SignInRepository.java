package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.SignIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 17:21
 */
@Repository
public interface SignInRepository extends JpaRepository<SignIn, Long> {

    List<SignIn> findByOpenIdInAndStatus(List<String> openId, Integer status);
}
