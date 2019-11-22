package com.huatu.tiku.teacher.service.common;

import com.huatu.tiku.entity.common.Area;
import com.huatu.tiku.response.area.AreaTreeResp;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * 地区
 * Created by x6 on 2018/5/8.
 */
public interface AreaService extends BaseService<Area> {

    /**
     * 返回全量地区树
     * @return
     */
    List<AreaTreeResp> areaList();

    /**
     * 根据科目返回地区树
     * @param subject
     * @return
     */
    List<AreaTreeResp> findAreaBySubject(Long subject);

    /**
     * 批量查询地区名称
     * @param areaIds
     * @return
     */
    List<String> findNameByIds(List<Long> areaIds);

    /**
     * 批量查询地区
     * @param areaIds
     * @return
     */
    List<Area> findByIds(List<Long> areaIds);
}
