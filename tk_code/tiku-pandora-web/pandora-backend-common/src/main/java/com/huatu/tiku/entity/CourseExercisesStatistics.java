package com.huatu.tiku.entity;


import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;


/**
 * 课后作业统计表
 */

@Data
@NoArgsConstructor
@Table(name = "course_exercises_statistics")
public class CourseExercisesStatistics extends BaseEntity {

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课程类型 直播 or 录播
     */
    private Integer courseType;

    /**
     * 班级总共答对题数
     */
    private Integer corrects;

    /**
     * 班级总共耗时 s
     */
    private Integer costs;

    /**
     * 统计人次
     */
    private Integer counts;

    /**
     * 课后作业试题数量
     */
    private Integer questionCount;
    
    /**
     * 0课后作业 1录播随堂练
     */
    private Integer type;
}