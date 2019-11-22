package com.huatu.tiku.schedule.biz.service.imple;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.Module;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.repository.ModuleRepository;
import com.huatu.tiku.schedule.biz.service.ModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangjian
 **/
@Service
@Slf4j
public class ModuleServiceImpl extends BaseServiceImpl<Module, Long> implements ModuleService {

    private final ModuleRepository moduleRepository;

    @Autowired
    public ModuleServiceImpl(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }
    @Override
    public List<Module> findByExamType(ExamType examType) {
        return moduleRepository.findByExamType(examType.getId());
    }

    @Override
    public List<Module> findBySubjectId(Long subjectId) {
        return moduleRepository.findBySubjectId(subjectId);
    }
}
