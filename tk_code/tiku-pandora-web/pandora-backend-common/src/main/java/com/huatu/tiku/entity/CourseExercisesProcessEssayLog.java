package com.huatu.tiku.entity;


import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;


/**
 * 申论课后作业提示表
 */
@Data
@NoArgsConstructor
@Table(name = "course_exercises_process_essay_log")
public class CourseExercisesProcessEssayLog extends BaseEntity {
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
     * 练习类型(0单题1套题)
     */
    private Integer questionType;

    /**
     * 试题id(type 是单题,此字段为试题Id;type 为套题，此字段为套卷Id)
     */
    private Long pQid;

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