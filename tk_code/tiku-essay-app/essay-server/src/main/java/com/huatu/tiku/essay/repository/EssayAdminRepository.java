package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zhouwei
 * @Description: 管理员相关
 * @create 2017-12-13 下午6:50
 **/
public interface EssayAdminRepository extends JpaRepository<EssayAdmin,Long> {
       EssayAdmin findByNameAndPassword(String name,String password);
}
