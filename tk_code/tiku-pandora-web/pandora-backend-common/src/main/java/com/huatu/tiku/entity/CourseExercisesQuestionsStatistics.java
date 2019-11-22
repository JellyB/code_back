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
@Table(name = "course_exercises_question_statistics")
public class CourseExercisesQuestionsStatistics extends BaseEntity {

    /**
     * 统计信息主表 id
     */
    private Long statisticsId;

    /**
     * 试题 id
     */
    private Long questionId;

    /**
     * 答对人数
     */
    private Integer corrects;

    /**
     * 答题人数
     */
    private Integer counts;

}