package com.huatu.tiku.entity;


import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;


@Data
@NoArgsConstructor
@Table(name = "course_exercises_card_info")
public class CourseExercisesCardInfo extends BaseEntity {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 课程类型 直播 or 录播
     */
    private Integer courseType;

    /**
     * 大纲id
     */
    private Long syllabusId;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课件id
     */
    private Long lessonId;

    /**
     * 答题卡id
     */
    private Long cardId;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 数据信息json格式
     */
    private String dataInfo;

    /**
     * 数据完成信息
     */
    private String completeInfo;

    /**
     * 是否提醒,0不提醒1提醒
     */
    private Integer isAlert;
}