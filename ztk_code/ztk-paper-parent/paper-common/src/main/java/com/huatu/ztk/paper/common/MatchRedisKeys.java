package com.huatu.ztk.paper.common;

public class MatchRedisKeys {


    public static final  String getMatchBySubjectRedisKey(int subject){
        return "m_s_"+subject;
    }


    /**
     * 职位分数zset
     *
     * @param paperId
     * @param positionId
     * @return
     */
    public static final String getPositionPracticeIdSore(int paperId,int positionId){
        StringBuilder stringBuilder = new StringBuilder("position_pi_score_").append(paperId)
                .append("_").append(positionId);
        return stringBuilder.toString();
    }


    /**
     * 学院分数zset
     *
     * @param paperId
     * @param schoolId
     * @return
     */
    public static final String getSchoolPracticeIdSore(int paperId,long schoolId){
        StringBuilder stringBuilder = new StringBuilder("school_pi_score_").append(paperId)
                .append("_").append(schoolId);
        return stringBuilder.toString();
    }

    /**
     * 职位总分
     *
     * @param paperId
     * @param positionId
     * @return
     */
    public static final String getPositionScoreSum(int paperId,int positionId){
        StringBuilder stringBuilder = new StringBuilder("position_score_").append(paperId)
                .append("_").append(positionId);
        return stringBuilder.toString();
    }

    /**
     * 某一个职位的报名set key,用来统计职位的报名人数
     * @param paperId
     * @param positionId
     * @return
     */
    public static final String getPositionEnrollSetKey(int paperId, int positionId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("position_").append(paperId).append("_").append(positionId).toString();
    }

    /**
     * 某一个学院的报名set key,用来统计学院的报名人数
     */
    public static final String getSchoolEnrollSetKey(int paperId, long schoolId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("school_").append(paperId).append("_").append(schoolId).toString();
    }

    /**
     * 报名人数 value key
     * @param paperId
     * @return
     */
    public static final String getTotalEnrollCountKey(int paperId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("enroll_count_").append(paperId).toString();
    }


//    /**
//     * 自动提交的答题卡id
//     * @param paperId
//     * @return
//     */
//    public static final String getMatchAutoSubmitSetKey(int paperId) {
//        StringBuilder sb = new StringBuilder();
//        return sb.append("match_auto_submit_").append(paperId).toString();
//    }


    /**
     * 模考大赛答题卡 set
     * @param paperId
     * @return
     */
    public static final String getMatchPracticeIdSetKey(int paperId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("match_practice_").append(paperId).toString();
    }



    public static final String getMatchAutoSubmitLockKey() {
        return "match_auto_submit_lock";
    }


    /**
     * 模考大赛交卷的set
     * @param paperId
     * @return
     */
    public static final String getMatchSubmitPracticeIdSetKey(int paperId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("match_submit_").append(paperId).toString();
    }

    /**
     * 模考大赛行测报告数据缓存
     */
    public static final String getLineTestMatchReportKey(int paperId,long userId){
        StringBuilder sb = new StringBuilder();
        return  sb.append("l_t_m_r_1228_").append(paperId).append("_").append(userId).toString();
    }
    /**
     * 模考大赛总体成绩报告数据缓存
     */
    public static final String getMatchWithEssayReportKey(int paperId,int userId){
        StringBuilder sb = new StringBuilder();
        return  sb.append("m_w_e_r_1228_").append(paperId).append("_").append(userId).toString();
    }

    /**
     * 模考大赛中 用户白名单ID 信息
     * updateBy lijun
     */
    public static final String getMatchWhitUserReportKey(){
        return "match_white_key_2_0_1_8";
    }

    public static String getMatchInfoForDegradeKey(int subject) {
        StringBuilder sb = new StringBuilder();
        return sb.append("match_info_degrade_").append(subject).toString();
    }

    public static String getPcMatchInfoForDegradeKey(int subject) {
        StringBuilder sb = new StringBuilder();
        return sb.append("pc_match_info_degrade_").append(subject).toString();
    }

    /**
     * 模考大赛交卷总人数（报告生成后生效）
     * @param matchId
     * @return
     */
    public static String getMatchSubmitTotalKey(int matchId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("match_submit_total_").append(matchId).toString();
    }
}
