package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.Module;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import java.util.List;

/**
 * @author wangjian
 **/
public interface ModuleService extends BaseService<Module, Long> {

    List<Module> findByExamType(ExamType examType);

    List<Module> findBySubjectId(Long subjectId);
}
