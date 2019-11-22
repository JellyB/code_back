package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.ClassInfo;
import com.huatu.tiku.interview.entity.vo.response.AreaClassVO;

import java.util.Date;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/25 20:02
 * @Description
 */
public interface ClassInfoService {
    List<ClassInfo> getList();
    List<ClassInfo> getListByTime(Date date);
    ClassInfo getOne(Long id);

    List<ClassInfo> getListByArea(Long areaId);

    List<AreaClassVO> getClassListWithArea();
}
