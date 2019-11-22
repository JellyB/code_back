package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.dto.CreatFeedBackDto;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;


/**
 * @author wangjian
 **/
public interface ClassHourFeedbackService extends BaseService<ClassHourFeedback, Long> {

    void saveX(CreatFeedBackDto dto);

    List<ClassHourFeedback> check(ExamType examType, Long subjectId, Integer year, Integer month);

    Page<ClassHourFeedback> findClassHourFeedback(ExamType examType, Long subjectId, Integer year, Integer month, FeedbackStatus status, Pageable page);

    List<Map> importExcel(List<List<List<String>>> list);
}
