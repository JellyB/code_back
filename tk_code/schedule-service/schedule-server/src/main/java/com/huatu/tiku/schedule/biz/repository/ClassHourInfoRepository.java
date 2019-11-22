package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface ClassHourInfoRepository extends BaseRepository<ClassHourInfo, Long> {
    @Query(nativeQuery = true,value = "SELECT i.* FROM class_hour_info i LEFT JOIN class_hour_feedback f ON f.id = i.feedback_id WHERE i.teacher_id = ?3 AND f.date >= ?1 AND f.date <= ?2 AND f. STATUS = 2")
    List<ClassHourInfo> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId);
}
