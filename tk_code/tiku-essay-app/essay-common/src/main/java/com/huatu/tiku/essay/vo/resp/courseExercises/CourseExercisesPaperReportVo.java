package com.huatu.tiku.essay.vo.resp.courseExercises;

import com.huatu.tiku.essay.vo.resp.EssayPaperReportQuestionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 套题vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseExercisesPaperReportVo extends CourseExercisesCommonReportVo {

    //题目数量
    private int questionCount;

    //未作答题目数
    private int unfinishedCount;

    //总用时
    private int spendTime;

    //题目列表
    private List<EssayPaperReportQuestionVO> questionVOList;

    //试卷ID
    private long paperId;


}
