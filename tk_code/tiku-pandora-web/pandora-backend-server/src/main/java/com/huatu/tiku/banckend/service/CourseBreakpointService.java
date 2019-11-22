package com.huatu.tiku.banckend.service;

import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by lijun on 2018/6/11
 */
public interface CourseBreakpointService extends BaseService<CourseBreakpoint> {

    /**
     * 根据id 删除信息
     *
     * @param id 需要删除的ID
     * @return
     */
    void deleteById(Long id);

    /**
     * 修改排列顺序
     *
     * @param list 待修改数据集合
     * @return
     */
    void changeSort(List<CourseBreakpoint> list);

    /**
     * 根据ID 批量删除
     *
     * @param idList
     */
    void deleteByIdList(List<Long> idList);

    /**
     * 列表查询
     *
     * @param courseType 类型
     * @param courseId   ID
     * @param pointName  名称
     * @param position   位置
     * @return
     */
    List<CourseBreakpoint> listData(int courseType, long courseId, String pointName, long position);

}
