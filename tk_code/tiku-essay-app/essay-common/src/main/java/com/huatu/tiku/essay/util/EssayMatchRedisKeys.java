package com.huatu.tiku.essay.util;

public class EssayMatchRedisKeys {


//    /**
//     * 职位分数zset
//     *
//     * @param paperId
//     * @param positionId
//     * @return
//     */
//    public static final String getPositionPracticeIdSore(int paperId,int positionId){
//        StringBuilder stringBuilder = new StringBuilder("position_pi_score_").append(paperId)
//                .append("_").append(positionId);
//        return stringBuilder.toString();
//    }
//
//
//    /**
//     * 职位总分
//     *
//     * @param paperId
//     * @param positionId
//     * @return
//     */
//    public static final String getPositionScoreSum(int paperId,int positionId){
//        StringBuilder stringBuilder = new StringBuilder("position_score_").append(paperId)
//                .append("_").append(positionId);
//        return stringBuilder.toString();
//    }
//
//    /**
//     * 某一个职位的报名set key,用来统计职位的报名人数
//     * @param paperId
//     * @param positionId
//     * @return
//     */
//    public static final String getPostionEnrollSetKey(int paperId, int positionId) {
//        StringBuilder sb = new StringBuilder();
//        return sb.append("position_").append(paperId).append("_").append(positionId).toString();
//    }

    /**
     * 模考报名人数 value key
     * @param paperId
     * @return
     */
    public static final String getTotalEnrollCountKey(long paperId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("essay_enroll_count_").append(paperId).toString();
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

//
//    /**
//     * 模考大赛答题卡 set
//     * @param paperId
//     * @return
//     */
//    public static final String getMatchPracticeIdSetKey(int paperId) {
//        StringBuilder sb = new StringBuilder();
//        return sb.append("match_practice_").append(paperId).toString();
//    }
//
//
//
//    public static final String getMatchAutoSubmitLockKey() {
//        return "match_auto_submit_lock";
//    }
//
//
//    /**
//     * 模考大赛交卷的set
//     * @param paperId
//     * @return
//     */
//    public static final String getMatchSubmitPracticeIdSetKey(int paperId) {
//        StringBuilder sb = new StringBuilder();
//        return sb.append("match_submit_").append(paperId).toString();
//    }
}
