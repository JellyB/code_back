package com.huatu.tiku.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新php端练习数量VO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateExerciseNumReqVO {
    /**
     * 课程类型
     */
    private Integer courseType;
    /**
     * 课程id
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
     * 试题类型
     */
    private Integer questionType;

    /**
     * 科目类型
     */
    private Integer subjectType;


}
