package com.huatu.splider.service;

import com.huatu.common.jpa.core.BaseService;
import com.huatu.splider.dao.jpa.api.FbCourseDao;
import com.huatu.splider.dao.jpa.entity.FbCourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @author hanchao
 * @date 2018/2/27 16:16
 */
@Service
public class FbCourseService extends BaseService<FbCourse,Integer> {
    @Autowired
    private FbCourseDao fbCourseDao;
    @Override
    public JpaRepository<FbCourse, Integer> getDefaultDao() {
        return fbCourseDao;
    }

    public void deleteAll(){
        fbCourseDao.deleteAll();
    }
}
