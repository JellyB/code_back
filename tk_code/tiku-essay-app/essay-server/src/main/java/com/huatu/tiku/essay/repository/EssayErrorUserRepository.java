package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.ErrorUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zhouwei
 * @Description: 管理员相关
 * @create 2017-12-13 下午6:50
 **/
public interface EssayErrorUserRepository extends JpaRepository<ErrorUser,Long> {
}
