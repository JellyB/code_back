package com.huatu.ztk.paper.common;

/**
 * Created by huangqingpeng on 2019/2/20.
 */
public class SmallRedisKey {

    /**
     * 模考大赛答题卡 set
     * @param paperId
     * @return
     */
    public static final String getCreateAnswerCardCount(int paperId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("answer_card_create_").append(paperId).toString();
    }

    /**
     * 当天的小模考试卷信息缓存
     * @param subject
     * @param todayKey
     * @return
     */
    public static String getTodaySmallEstimate(int subject, String todayKey) {
        StringBuilder stringBuilder = new StringBuilder("small_estimate_paper_").append(subject).append("_").append(todayKey);
        return stringBuilder.toString();
    }

    /**
     * 往期解析课ID集合
     * @param subject
     * @return
     */
    public static String getSmallEstimateCourseIds(int subject) {
        StringBuilder stringBuilder = new StringBuilder("small_estimate_paper_course_ids").append("_").append(subject);
        return stringBuilder.toString();
    }

    /**
     * 获取答题报告数据缓存
     * @param id
     * @return
     */
    public static String getAnswerInfoKey(long id) {
        StringBuilder stringBuilder = new StringBuilder("paper_report").append("_").append(id);
        return stringBuilder.toString();
    }
}
