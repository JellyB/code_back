package com.huatu.splider.service;

import com.huatu.common.jpa.core.BaseService;
import com.huatu.splider.dao.jpa.api.FbTeacherDao;
import com.huatu.splider.dao.jpa.entity.FbTeacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @author hanchao
 * @date 2018/3/12 14:40
 */
@Service
public class FbTeacherService extends BaseService<FbTeacher,Integer>{

    @Autowired
    private FbTeacherDao fbTeacherDao;
    @Override
    public JpaRepository<FbTeacher, Integer> getDefaultDao() {
        return fbTeacherDao;
    }

}
