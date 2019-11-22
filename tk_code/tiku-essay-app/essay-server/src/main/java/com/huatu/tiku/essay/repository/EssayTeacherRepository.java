package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by duanxiangchao on 2019/7/9
 */
public interface EssayTeacherRepository extends JpaRepository<EssayTeacher, Long>, JpaSpecificationExecutor<EssayTeacher> {

    /**
     * 根据权限中心用户名获取老师信息
     *
     * @param uCenterName
     * @return
     */
    EssayTeacher findByUCenterName(String uCenterName);

    EssayTeacher findByUCenterId(Long uCenterId);

    EssayTeacher findByIdAndStatus(long teacherId, int status);



}
