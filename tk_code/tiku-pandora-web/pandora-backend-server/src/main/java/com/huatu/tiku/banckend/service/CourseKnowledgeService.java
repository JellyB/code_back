package com.huatu.tiku.banckend.service;

import com.huatu.tiku.dto.request.BatchKnowledgeByCourseReqVO;
import com.huatu.tiku.entity.CourseKnowledge;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by lijun on 2018/6/11
 */
public interface CourseKnowledgeService extends BaseService<CourseKnowledge> {

    /**
     * 编辑 课程关联知识点信息
     *
     * @param courseType        课程类型
     * @param courseId          课程ID
     * @param knowledgePointIds 知识点ID，使用'，'分隔
     */
    void edit(Integer courseType, long courseId, String knowledgePointIds);

    /**
     * 通过课程ID 查询关联的知识点ID 信息
     *
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    List<Long> getListByCourseId(Integer courseType, long courseId);

    /**
     * 移除所有的绑定关系
     *
     * @param courseType 课程类型
     * @param courseId   课程ID
     */
    void removeAllByCourseId(Integer courseType, long courseId);

    List<BatchKnowledgeByCourseReqVO> getBatchListByCourseId(List<BatchKnowledgeByCourseReqVO> list);

    void boundKnowledgeByQuestion(Long courseId, Integer courseType, Long questionId);
}
