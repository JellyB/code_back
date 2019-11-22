package com.huatu.tiku.banckend.service;

import com.huatu.tiku.entity.CourseExercisesQuestion;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by lijun on 2018/6/12
 */
public interface CourseExercisesService extends BaseService<CourseExercisesQuestion> {

    /**
     * 删除课后练习的关联信息
     *
     * @param courseId   课程ID
     * @param courseType 课程类型
     * @param questionId 试题ID
     * @return
     */
    int deleteQuestion(Long courseId, Integer courseType, Long questionId);

    /**
     * 重新排序
     *
     * @param courseId   课程ID
     * @param courseType 课程类型
     * @param list       排序集合
     * @return
     */
    int changeSort(Long courseId, Integer courseType, List<CourseExercisesQuestion> list);

    /**
     * 根据课件ID 删除课件下绑定的所有试题
     * 用于课件绑定课件练习,清除数据
     */
    int deleteQuestionByCourseId(Long courseWareId, Integer courseType);
}
