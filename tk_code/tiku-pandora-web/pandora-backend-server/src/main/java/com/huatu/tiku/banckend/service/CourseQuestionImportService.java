package com.huatu.tiku.banckend.service;

import com.huatu.tiku.dto.request.CourseQuestionImportVO;
import com.huatu.ztk.commons.exception.BizException;


public interface CourseQuestionImportService {

    void batchImport(CourseQuestionImportVO vo,Long userId) throws BizException;

    String getSubjectByCategory(Long category);

    Object getAllSubject();

    void synchronizeCourse(String courseIds,Long userId);
}
