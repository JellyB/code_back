package com.huatu.tiku.position.biz.controller;

import com.alicp.jetcache.anno.Cached;
import com.google.common.collect.Lists;
import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.enums.Status;
import com.huatu.tiku.position.biz.service.AreaService;
import com.huatu.tiku.position.biz.vo.AreaVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@RestController
@RequestMapping("area")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @RequestMapping("getAreasByID")
    @Cached(name = JetCacheConstant.AREA_CONTROLLER_FIND_BY_PARENT_ID_NAME,
            key = JetCacheConstant.AREA_CONTROLLER_FIND_BY_PARENT_ID_KEY,
            expire = 6 * 60 * 60)
    public List<AreaVo> getAreasByID(@RequestParam(defaultValue = "-1") Long areaId, @RequestParam(defaultValue = "true") Boolean noLimit) {
        if (-1 != areaId) {
            return areaService.findByParentId(areaId).stream().map(AreaVo::new).collect(Collectors.toList());
        } else {
            //没选值
            List<Area> areas = areaService.getProvinceArea();

            List<AreaVo> list = areas.stream().map(AreaVo::new).collect(Collectors.toList());

            List<AreaVo> result = Lists.newArrayListWithExpectedSize(areas.size() + 1);

            if (noLimit) {
                Area empty = new Area();
                empty.setId(0L);
                empty.setName("不限");
                empty.setCode("0");
                empty.setType(1);
                empty.setStatus(Status.ZC);

                result.add(new AreaVo(empty));
                result.addAll(list);

                return result;
            } else {
                return list;
            }
        }
    }
}
