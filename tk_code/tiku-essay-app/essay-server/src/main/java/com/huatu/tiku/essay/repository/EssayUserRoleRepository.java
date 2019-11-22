package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayUserRole;
import com.huatu.tiku.essay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author jbzm
 * @date Create on 2018/3/12 12:01
 */
public interface EssayUserRoleRepository extends JpaRepository<EssayUserRole, Long> {
    List<EssayUserRole> findByUserId(Long userId);
}
