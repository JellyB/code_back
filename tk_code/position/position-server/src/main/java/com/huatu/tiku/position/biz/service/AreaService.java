package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.Area;

import java.util.List;

/**
 * @author wangjian
 **/
public interface AreaService extends BaseService <Area,Long>{

    //将子集的id添加进集合
    List<Long> getAllByIds(List<Long> parentIds);

    //取出所有省级地区
    List<Area> getProvinceArea();

    //根据上级地区查找
    List<Area> findByParentId(Long provinceId);

    //根据上级地区ids查找
    List<Area> findByParentIds(List<Long> provinceIds);
}
