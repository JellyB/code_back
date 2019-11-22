package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述 申论人工批改单题报告相关redis-key
 */
public class QuestionReportRedisKeyConstant {

    /**
     * 所有成绩排名
     *
     * @param
     * @return
     */
    public static String getQuestionReportScoreZsetKey(Long questionId) {
        return Joiner.on("_").join("essay_question_report_score_zset", questionId);
    }

    /**
     * 班级下课后作业成绩排名
     *
     * @param questionId
     * @param syllabusId
     * @return
     */
    public static String getExercisesQuestionReportScoreZsetKey(Long questionId, Long syllabusId) {
        return Joiner.on("_").join("essay_execrises_question_report_score_zset", questionId, syllabusId);
    }

    /**
     * 所有成绩累计
     *
     * @param
     * @return
     */
    public static String getQuestionReportScoreSumKey(Long questionId) {
        return Joiner.on("_").join("essay_question_report_score_sum", questionId);
    }

    /**
     * 班级下课后作业所有成绩累计
     *
     * @param
     * @return
     */
    public static String getExercisesQuestionReportScoreSumKey(Long questionId, Long syllabusId) {
        return Joiner.on("_").join("essay_question_report_score_sum", questionId, syllabusId);
    }

    /**
     * 班级下课后作业总作答时长
     *
     * @param questionId
     * @param syllabusId
     * @return
     */
    public static String getExercisesQuestionReportSpendTimeSumKey(Long questionId, Long syllabusId) {
        return Joiner.on("_").join("essay_question_report_spend_time_sum", questionId, syllabusId);
    }

    /**
     * 报告缓存
     *
     * @param
     * @return
     */
    public static String getQuestionReportKey(Long answerId) {
        return Joiner.on("_").join("essay_question_report_", answerId);
    }

    /**
     * 智能转人工此水记录缓存
     *
     * @param answerId
     * @param answerType
     * @return
     */
    public static String getPaperConvertCount(int answerType, Long answerId) {
        return Joiner.on("_").join("essay_paper_convert_count_", answerType, answerId);
    }


    /**
     * 试卷班级 下课后作业成绩排名
     *
     * @param paperOrQuestionId
     * @param syllabusId
     * @return
     */
    public static String getExercisesPaperReportScoreZsetKey(Long paperOrQuestionId, Long syllabusId, Integer answerType) {
//        return Joiner.on("_").join("essay_exercise_paper_report_score_zset", paperOrQuestionId, syllabusId, answerType);
        if(EssayAnswerCardEnum.TypeEnum.PAPER.getType() == answerType.intValue()){
            return PaperReportRedisKeyConstant.getExercisesPaperReportScoreZsetKey(paperOrQuestionId,syllabusId);
        }else{
            return QuestionReportRedisKeyConstant.getExercisesQuestionReportScoreZsetKey(paperOrQuestionId,syllabusId);
        }
    }

    /**
     * 试卷班级 下课后作业所有成绩累计
     *
     * @param paperOrQuestionId
     * @return
     */
    public static String getExercisesPaperReportScoreSumKey(Long paperOrQuestionId, Long syllabusId, Integer answerType) {
//        return Joiner.on("_").join("essay_paper_report_score_sum", paperOrQuestionId, syllabusId, answerType);
        if(EssayAnswerCardEnum.TypeEnum.PAPER.getType() == answerType.intValue()){
            return PaperReportRedisKeyConstant.getExercisesPaperReportScoreSumKey(paperOrQuestionId,syllabusId);
        }else{
            return QuestionReportRedisKeyConstant.getExercisesQuestionReportScoreSumKey(paperOrQuestionId,syllabusId);
        }
    }

    /**
     * 试卷班级 下课后作业总作答时长
     *
     * @param paperOrQuestionId
     * @param syllabusId
     * @return
     */
    public static String getExercisesPaperSpendTimeSumKey(Long paperOrQuestionId, Long syllabusId, Integer answerType) {
//        return Joiner.on("_").join("essay_paper_report_spend_time_sum", paperOrQuestionId, syllabusId, answerType);
        if(EssayAnswerCardEnum.TypeEnum.PAPER.getType() == answerType.intValue()){
            return PaperReportRedisKeyConstant.getExercisesPaperReportSpendTimeSumKey(paperOrQuestionId,syllabusId);
        }else{
            return QuestionReportRedisKeyConstant.getExercisesQuestionReportSpendTimeSumKey(paperOrQuestionId,syllabusId);
        }
    }


}
