package com.huatu.tiku.essay.vo.admin.courseExercise;

import lombok.Builder;
import lombok.Data;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/28
 * @描述
 */

@Data
@Builder
public class AdminUpdateExerciseNumReqVO {

    /**
     * 课程类型
     */
    private Integer courseType;
    /**
     * 课件ID
     */
    private Long classId;
    /**
     * 随堂练习题目数
     */
    private Integer classExercisesNum;

    /**
     * 课后练习个数
     */
    private Integer afterExercisesNum;

    /**
     * 试题类型(单题 套题 多题)
     */
    private Integer buildType;

    /**
     * 科目类型
     */
    private Integer subjectType;


}
