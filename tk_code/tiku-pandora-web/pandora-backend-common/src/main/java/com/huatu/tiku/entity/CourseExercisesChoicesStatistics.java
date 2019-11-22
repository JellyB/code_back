package com.huatu.tiku.entity;


import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;


/**
 * 课后作业选项统计表
 */

@Data
@NoArgsConstructor
@Table(name = "course_exercises_choices_statistics")
public class CourseExercisesChoicesStatistics extends BaseEntity {

    /**
     * 试题 id
     */
    private Long questionId;

    /**
     * 选项
     */
    private Integer choice;

    /**
     * 选择次数
     */
    private Integer counts;

}