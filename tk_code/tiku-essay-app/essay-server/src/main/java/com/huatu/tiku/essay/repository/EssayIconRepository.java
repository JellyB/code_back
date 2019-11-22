package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayIcon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 *
 * 申论首页icon管理
 * Created by x6 on 2018/6/14.
 */
public interface EssayIconRepository extends JpaRepository<EssayIcon, Long>, JpaSpecificationExecutor<EssayIcon> {

    /**
     * 根据状态查询
     * @param status
     * @return
     */
    List<EssayIcon> findByStatus(Integer status);


}
