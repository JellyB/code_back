package com.huatu.tiku.course.service.v1;

import com.huatu.tiku.entity.CourseExercisesQuestion;
import service.BaseServiceHelper;

import java.util.List;
import java.util.Map;

/**
 * 课程-课后练习
 * Created by lijun on 2018/6/21
 */
public interface CourseExercisesService extends BaseServiceHelper<CourseExercisesQuestion> {

    /**
     * 通过课程ID、类型 查询课后练习信息
     *
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    List<Map<String, Object>> listQuestionByCourseId(Integer courseType, Long courseId);

}
