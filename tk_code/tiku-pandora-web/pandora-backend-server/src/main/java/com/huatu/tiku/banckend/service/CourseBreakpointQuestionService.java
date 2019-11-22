package com.huatu.tiku.banckend.service;

import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * Created by lijun on 2018/6/12
 */
public interface CourseBreakpointQuestionService extends BaseService<CourseBreakpointQuestion> {

    /**
     * 根据断点ID 删除数据
     *
     * @param breakpointId 课程ID
     * @return 操作成功数量
     */
    int deleteByBreakpointId(Long breakpointId);

    /**
     * 根据ID 批量删除
     * @param breakpointIdList 批量ID
     * @return
     */
    int deleteByBreakpointIdList(List<Long> breakpointIdList);

    /**
     * 修改排列序号
     *
     * @param courseBreakpointId 待修改的断点ID
     * @param list               待修改数据
     */
    void changeSort(Long courseBreakpointId, List<CourseBreakpointQuestion> list);

    /**
     * 根据结点ID 查询题目个数
     *
     * @param pointIdList 结点id
     * @return 题目个数
     */
    int selectCountByPointIdList(List<Long> pointIdList);

    /**
     * 根据节点ID 查询各个节点下的question 数量
     */
    Map<Long,Long> countQuestionNumGroupById(List<Long> pointIdList);

}
