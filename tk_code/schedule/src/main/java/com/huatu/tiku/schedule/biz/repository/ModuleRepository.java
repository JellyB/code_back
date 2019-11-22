package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Module;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangjian
 **/
public interface ModuleRepository extends BaseRepository<Module, Long> {

    @Query(value = "SELECT m.* from module m LEFT JOIN `subject` s on m.subject_id=s.id where s.exam_type=?1",nativeQuery = true)
    List<Module> findByExamType(int examType);

    List<Module> findBySubjectId(Long subjectId);
}
