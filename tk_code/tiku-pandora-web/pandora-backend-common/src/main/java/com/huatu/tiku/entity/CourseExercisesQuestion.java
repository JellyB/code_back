package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 课程 - 课后练习 试题关联表
 * Created by lijun on 2018/6/10
 */
@Data
@NoArgsConstructor
@Table(name = "course_exercises_question")
public class CourseExercisesQuestion extends BaseEntity {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 课程类型
     */
    @NotNull(message = "课程类型不能为空")
    private Integer courseType;

    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 试题ID
     */
    @NotNull(message = "试题ID不能为空")
    private Long questionId;

    /**
     * 排列序号
     */
    private Integer sort;


    @Builder
    public CourseExercisesQuestion(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long courseId, Integer courseType, Long userId, Long questionId, Integer sort) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.courseId = courseId;
        this.courseType = courseType;
        this.userId = userId;
        this.questionId = questionId;
        this.sort = sort;
    }
}
