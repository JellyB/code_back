package com.huatu.tiku.course.service.v1;

import com.huatu.tiku.course.bean.CourseBreakpointQuestionDTO;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import service.BaseServiceHelper;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lijun on 2018/6/19
 */
public interface CourseBreakpointService extends BaseServiceHelper<CourseBreakpoint> {

    /**
     * 获取某个课程下的所有试题信息
     *
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    List<CourseBreakpointQuestionDTO> listAllQuestionId(int courseType, long courseId);

    /**
     * 获取所有断点信息
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    List<CourseBreakpoint> listByCourseTypeAndId(Integer courseType, Long courseId);

    /**
     * 获取所有断点信息
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    List<CourseBreakpointQuestion> listQuestionByCourseTypeAndId(Integer courseType, Long courseId);

    /**
     * 创建答题卡
     *
     * @param terminal   设备类型
     * @param subjectId  科目ID
     * @param userId     用户ID
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    HashMap<String, Object> buildCard(int terminal, int subjectId, int userId, int courseType, long courseId);
}
