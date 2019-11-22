package com.huatu.tiku.position.biz.service.impl;

import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.respository.AreaRepository;
import com.huatu.tiku.position.biz.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@Service
public class AreaServiceImpl extends BaseServiceImpl<Area,Long> implements AreaService {

    private final AreaRepository areaRepository;

    @Autowired
    public AreaServiceImpl(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    //取出全部子集
    public List<Long> getAllByIds(List<Long> parentIds){
        List<Area> areas = areaRepository.findByParentIdIn(parentIds);
        if(null!=areas&&!areas.isEmpty()){//不为空时 取出子集的id 添加进结果集 并将结果集再次查询子集
            List<Long> ids=areas.stream().map(area->area.getId()).collect(Collectors.toList());
            parentIds.addAll(ids);
            parentIds.addAll(areaRepository.findByParentIdIn(ids).stream().map(area->area.getId()).collect(Collectors.toList()));
        }
        return parentIds;
    }

    @Override
    public List<Area> getProvinceArea() {
        final Integer type=1;
        return areaRepository.findByType(type);
    }

    @Override
    public List<Area> findByParentId(Long provinceId) {
        return areaRepository.findByParentId(provinceId);
    }

    @Override
    public List<Area> findByParentIds(List<Long> provinceIds) {
        return areaRepository.findByParentIdIn(provinceIds);
    }
}
