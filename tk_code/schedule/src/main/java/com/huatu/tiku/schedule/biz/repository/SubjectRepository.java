package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import java.util.List;

/**
 * 科目Repository
 * 
 * @author Geek-S
 *
 */
public interface SubjectRepository extends BaseRepository<Subject, Long> {

    /**
     * 根据考试类型查询科目
     *
     * @param examType 考试类型
     * @return 科目列表
     */
    List<Subject> findByExamTypeAndShowFlagIsTrue(ExamType examType);

    /**
     * 根据考试类型查询科目
     *
     * @param id 科目id
     * @return 科目列表
     */
    List<Subject> findById(Long id);

}
