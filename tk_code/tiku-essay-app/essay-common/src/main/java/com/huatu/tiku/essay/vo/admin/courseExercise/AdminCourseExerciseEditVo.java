package com.huatu.tiku.essay.vo.admin.courseExercise;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述 课后作业编辑vo
 */
@Data
public class AdminCourseExerciseEditVo {

    /**
     * id
     */
    private Long id;

    /**
     * 序号
     */
    private Integer sort;

  /*  *//**
     * 单次批改费用
     *//*
    @NotNull(message = "批改费用不能为空!")
    private Double correctPrice;*/

}
