package com.huatu.splider.dao.jpa.api;

import com.huatu.splider.dao.jpa.entity.FbCourseSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author hanchao
 * @date 2018/2/27 16:08
 */
public interface FbCourseSnapshotDao extends JpaRepository<FbCourseSnapshot,Integer> {
    @Query(value = " select * from fb_course_snapshot where course_id=? order by id desc limit 1 ",nativeQuery = true)
    FbCourseSnapshot getLastestSnapshot(int courseId);
}
