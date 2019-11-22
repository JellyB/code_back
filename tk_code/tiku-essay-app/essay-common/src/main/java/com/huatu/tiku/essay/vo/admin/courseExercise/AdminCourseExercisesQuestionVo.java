package com.huatu.tiku.essay.vo.admin.courseExercise;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述
 */
public class AdminCourseExercisesQuestionVo {


    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 大纲ID
     */
    private Long syllabusId;

    /**
     * 课件ID
     */
    private Long courseWareId;

    /**
     * 课程类型(直播录播)
     */
    private Integer courseType;

    /**
     * 试题id(type 是单题,此字段为试题Id;type 为套题，此字段为套卷Id)
     */
    private Long questionOrPaperId;

    /**
     * 练习类型(0单题1套题)
     */
    private Integer type;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 批改费用（元/次）
     */
    private Double correctPrice;

    private Long paperId;

    private Long questionId;


}
