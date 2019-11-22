package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import java.util.List;

/**
 * 科目Service
 * 
 * @author Geek-S
 *
 */
public interface SubjectService extends BaseService<Subject, Long> {

    /**
     * 根据考试类型查询科目
     *
     * @param examType 考试类型
     * @return 科目列表
     */
    List<Subject> findByExamType(ExamType examType);

    /**
     * 根据考试类型查询科目
     *
     * @param examType 考试类型
     * @return 科目列表
     */
    List<Subject> findByExamType(ExamType examType,Long subjectId);

}
