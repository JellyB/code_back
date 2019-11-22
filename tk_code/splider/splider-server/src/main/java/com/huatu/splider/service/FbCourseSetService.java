package com.huatu.splider.service;

import com.huatu.common.jpa.core.BaseService;
import com.huatu.splider.dao.jpa.api.FbCourseSetDao;
import com.huatu.splider.dao.jpa.entity.FbCourseSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @author hanchao
 * @date 2018/2/27 16:17
 */
@Service
public class FbCourseSetService extends BaseService<FbCourseSet,Integer> {
    @Autowired
    private FbCourseSetDao fbCourseSetDao;
    @Override
    public JpaRepository<FbCourseSet, Integer> getDefaultDao() {
        return fbCourseSetDao;
    }

    public void deleteAll(){
        fbCourseSetDao.deleteAll();
    }
}
