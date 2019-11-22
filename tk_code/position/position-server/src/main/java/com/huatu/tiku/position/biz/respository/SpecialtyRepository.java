package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.Specialty;
import com.huatu.tiku.position.biz.enums.Education;

import java.util.List;

/**
 * @author wangjian
 **/
public interface SpecialtyRepository extends BaseRepository<Specialty,Long> {

    List<Specialty> findByEducation(Education education);

    List<Specialty> findByNameLike(String name);

    List<Specialty> findByNameAndEducationIsGreaterThanEqual(String name,Education education);

}
