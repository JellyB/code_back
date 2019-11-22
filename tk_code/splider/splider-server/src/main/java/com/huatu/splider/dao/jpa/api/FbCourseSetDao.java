package com.huatu.splider.dao.jpa.api;

import com.huatu.splider.dao.jpa.entity.FbCourseSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author hanchao
 * @date 2018/2/27 16:08
 */
public interface FbCourseSetDao extends JpaRepository<FbCourseSet,Integer> {
    @Modifying
    @Query("update FbCourseSet set state=0")
    void deleteAll();
}
