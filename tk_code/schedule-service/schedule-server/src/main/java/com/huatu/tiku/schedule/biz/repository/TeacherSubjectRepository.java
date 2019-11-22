package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教师Repository
 *
 * @author Geek-S
 */
public interface TeacherSubjectRepository extends BaseRepository<TeacherSubject, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE from TeacherSubject where teacher.id = ?1")
    void deleteByTeacherId(Long id);

    TeacherSubject findByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
}
