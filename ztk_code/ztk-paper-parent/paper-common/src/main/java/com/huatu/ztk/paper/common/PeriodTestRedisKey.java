package com.huatu.ztk.paper.common;

/**
 * @author shanjigang
 * @date 2019/2/26 20:38
 */
public class PeriodTestRedisKey {
    /**
     * 获取阶段测试答题数目key
     * @param paperId
     * @return
     */
    public static final String getAnswerCardCount(int paperId, long syllabusId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("period_test_answer_card_count_").append(paperId).append("_").append(syllabusId).toString();
    }

    /**
     * 获取阶段测试报告缓存Key
     * @return
     */
    public static String getPeriodReportKey() {
        StringBuilder stringBuilder = new StringBuilder("period_test_report");
        return stringBuilder.toString();
    }

    /**
     * 获取阶段测试试卷key
     * @param paperId
     * @return
     */
    public static final String getPeriodTestPaper(int paperId,long syllabusId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("period_test_paper_").append(paperId).append("_").append(syllabusId).toString();
    }
    
    /**
     * 获取阶段测试自动交卷锁key
     * @return
     */
    public static final String getPeriodTestAutoSubmitLockKey() {
        return "period_test_auto_submit_lock";
    }
    
	/**
	 * 获取阶段测试未交卷的答题卡zset
	 * 
	 * @return
	 */
	public static final String getPeriodTestAnswerCardUnfinshKey() {
		return "period_test_answer_card_unfinsh";
	}

    /**
     * 获取阶段测试正确率人数key
     * @param paperId
     * @return
     */
    public static final String getPeriodTestAccuracyNum(int paperId,long syllabusId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("period_test_accuracy_num_").append(paperId).append("_").append(syllabusId).toString();
    }

    /**
     * 获取阶段测试正确率key
     * @param paperId
     * @return
     */
    public static final String getPeriodTestQuestionAccuracy(int paperId,long syllabusId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("period_test_question_accuracy_").append(paperId).append("_").append(syllabusId).toString();
    }

}
