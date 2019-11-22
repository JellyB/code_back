package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 * @author zhaoxi
 * @Description: 申论套题报告相关redis-key
 * @date 2018/12/68:26 PM
 */
public class PaperReportRedisKeyConstant {

    /**
     * 所有成绩排名
     * @param
     * @return
     */
    public static String getPaperReportScoreZsetKey(Long paperId) {
        return  Joiner.on("_").join("essay_paper_report_score_zset",paperId);
    }

    /**
     * 所有成绩累计
     * @param
     * @return
     */
    public static String getPaperReportScoreSumKey(Long paperId) {
        return  Joiner.on("_").join("essay_paper_report_score_sum",paperId);
    }
    /**
     * 报告缓存
     * @param
     * @return
     */
    public static String getPaperReportKey(Long answerId) {
        return  Joiner.on("_").join("essay_paper_report",answerId);
    }

    /**
     * 班级下课后作业成绩排名
     * @param paperId
     * @param syllabusId
     * @return
     */
    public static String getExercisesPaperReportScoreZsetKey(Long paperId, Long syllabusId) {
        return Joiner.on("_").join("essay_execrises_paper_report_score_zset", paperId, syllabusId);
    }

    /**
     * 班级下课后作业所有成绩累计
     *
     * @param
     * @return
     */
    public static String getExercisesPaperReportScoreSumKey(Long paperId, Long syllabusId) {
        return Joiner.on("_").join("essay_paper_report_score_sum", paperId, syllabusId);
    }

    /**
     * 班级下课后作业总作答时长
     * @param paperId
     * @param syllabusId
     * @return
     */
    public static String getExercisesPaperReportSpendTimeSumKey(Long paperId, Long syllabusId) {
        return Joiner.on("_").join("essay_paper_report_spend_time_sum", paperId, syllabusId);
    }
}
