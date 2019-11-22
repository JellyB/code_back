package com.huatu.tiku.essay.service.courseExercises;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.vo.resp.courseExercises.CourseExercisesPaperReportVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.CourseExercisesQuestionReportVo;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 课后作业相关
 */
public interface CourseExercisesReportService {


    /**
     * 单题报告
     *
     * @param answerId
     * @param
     * @return
     */
    CourseExercisesQuestionReportVo getQuestionReport(Long answerId,Long syllabusId);

    CourseExercisesQuestionReportVo getQuestionReportMock(Long answerId, Long syllabusId);

    /**
     * 课后作业答题卡排名数据添加
     *
     * @param questionAnswer
     */
    void addQuestionCourseReport(EssayQuestionAnswer questionAnswer);

    /**
     * 课后作业答题卡排名数据添加
     *
     * @param paperAnswer
     */
    void addPaperCourseReport(EssayPaperAnswer paperAnswer);

    /**
     * 套卷报告
     *
     * @param answerId
     * @return
     */
    CourseExercisesPaperReportVo getRealPaperReport(Long answerId,Long courseWareId);

}
