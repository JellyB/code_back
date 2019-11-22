package com.huatu.tiku.essay.vo.admin.courseExercise;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/4
 * @描述
 */
@Data
public class AdminCourseExerciseCleanDataVo {

    @NotNull(message = "课件Id不能为空！")
    private Long courseWareId;
    @NotNull(message = "课件类型不能为空！")
    private Integer courseType;


}
