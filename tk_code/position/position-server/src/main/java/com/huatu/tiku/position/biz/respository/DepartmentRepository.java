package com.huatu.tiku.position.biz.respository;


import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.Department;
import com.huatu.tiku.position.biz.enums.Nature;

import java.util.List;

/**
 * 部门
 *
 * @author wangjian
 **/
public interface DepartmentRepository extends BaseRepository<Department, Long> {

    List<Department> findByNatureAndCodeAndName(Nature nature, String code, String name);
}
