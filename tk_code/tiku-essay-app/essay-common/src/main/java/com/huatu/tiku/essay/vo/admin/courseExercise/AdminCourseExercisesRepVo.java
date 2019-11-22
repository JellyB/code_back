package com.huatu.tiku.essay.vo.admin.courseExercise;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述
 */
@Data
public class AdminCourseExercisesRepVo {


    @NotNull(message = "课件ID不能为空!")
    private Long courseWareId;

    /**
     * 绑定的试题ID或者试卷ID
     */
    @NotNull(message = "绑定的试题ID或者试卷ID不能为空！")
    private Long paperOrQuestionId;
    /**
     * 类型（0 单题 1 套题）
     */
    @NotNull(message = "试题类型不能为空!")
    private Integer type;

    /**
     * 课程类型（直播录播）
     */
    @NotNull(message = "课程类型不能为空!")
    private Integer courseType;

    /**
     * 批改方式
     */
    @NotNull(message = "课程类型不能为空!")
    private Integer correctMode;

    @NotNull(message = "批改价格不能为空！")
    private Double correctPrice;

}
