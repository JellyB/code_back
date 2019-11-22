package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import java.util.List;

/**
 * @author wangjian
 **/
public interface ClassHourFeedbackRepository extends BaseRepository<ClassHourFeedback, Long> {

    List<ClassHourFeedback> findByExamTypeAndSubjectIdAndYearAndMonthAndStatusIn(ExamType type, Long subjectId, Integer year, Integer month, List list);
}
