package com.huatu.splider.dao.jpa.api;

import com.huatu.splider.dao.jpa.entity.FbCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author hanchao
 * @date 2018/2/27 16:07
 */
public interface FbCourseDao extends JpaRepository<FbCourse,Integer> {
    @Modifying
    @Query("update FbCourse set state=0")
    void deleteAll();
}
