package com.huatu.tiku.course.service.v1.practice;

import com.huatu.tiku.course.bean.vo.CoursewarePracticeQuestionVo;
import com.huatu.tiku.entity.CoursewarePracticeQuestionInfo;

import service.BaseServiceHelper;

/**
 * @author shanjigang
 * @date 2019/3/23 18:56
 */
public interface CoursewarePracticeQuestionInfoService extends BaseServiceHelper<CoursewarePracticeQuestionInfo> {
    /**
     * 根据 roomId coursewareId 列表查询
     */
    CoursewarePracticeQuestionVo findByCoursewareIdAndRoomId(Long roomId, Long coursewareId);

    /**
     * 生成课件下试题作答信息
     * @param roomId roomId
     */
    void generateCoursewareAnswerCardInfo(Long roomId);
}
