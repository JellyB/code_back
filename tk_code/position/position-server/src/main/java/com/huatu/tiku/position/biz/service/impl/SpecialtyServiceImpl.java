package com.huatu.tiku.position.biz.service.impl;

import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.domain.Specialty;
import com.huatu.tiku.position.biz.enums.Education;
import com.huatu.tiku.position.biz.respository.SpecialtyRepository;
import com.huatu.tiku.position.biz.service.SpecialtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangjian
 **/
@Service
public class SpecialtyServiceImpl extends BaseServiceImpl<Specialty,Long> implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Autowired
    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    public List<Specialty> findByEducation(Education education) {
        return specialtyRepository.findByEducation(education);
    }
}
