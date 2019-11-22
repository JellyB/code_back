package com.huatu.tiku.essay.vo.resp.courseExercises;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单题报告vo
 * @author zhangchong
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseExercisesQuestionReportVo extends CourseExercisesCommonReportVo {



    //试题基础ID
    private long questionBaseId;
    //试题详情id
    private long questionDetailId;


}
