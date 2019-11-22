package com.huatu.splider.service;

import com.huatu.common.jpa.core.BaseService;
import com.huatu.splider.dao.jpa.api.FbCourseSnapshotDao;
import com.huatu.splider.dao.jpa.entity.FbCourseSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @author hanchao
 * @date 2018/2/27 16:18
 */
@Service
public class FbCourseSnapshotService extends BaseService<FbCourseSnapshot,Integer> {
    @Autowired
    private FbCourseSnapshotDao fbCourseSnapshotDao;
    @Override
    public JpaRepository<FbCourseSnapshot, Integer> getDefaultDao() {
        return fbCourseSnapshotDao;
    }

    public FbCourseSnapshot getLastestSnapshot(int courseId){
        return fbCourseSnapshotDao.getLastestSnapshot(courseId);
    }
}
