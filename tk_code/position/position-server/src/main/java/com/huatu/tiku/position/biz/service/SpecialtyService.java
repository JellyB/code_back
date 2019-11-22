package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.Specialty;
import com.huatu.tiku.position.biz.enums.Education;

import java.util.List;

/**
 * @author wangjian
 **/
public interface SpecialtyService extends BaseService<Specialty,Long> {


    /**
     * 通过学历查找专业
     */
    List<Specialty> findByEducation(Education education);
}
