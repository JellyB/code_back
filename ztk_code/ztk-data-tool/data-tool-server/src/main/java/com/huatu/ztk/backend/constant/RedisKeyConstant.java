package com.huatu.ztk.backend.constant;

/**
 * Created by huangqp on 2018\6\8 0008.
 */
public class RedisKeyConstant {

    /**
     * 获取下载试题的试题id
     * @return
     */
    public static String getDownloadId(Integer subject, Integer pointId){
        return "download_"+ subject +"_"+ pointId;
    }

    public static String getQuestionDownloadLock() {
        return "question_download_lock";
    }

    public static String getMatchCountLock() {
        return "match_count_lock";
    }

    /**
     * 模考大赛被导出报名数据的set
     * @return
     */
    public static String getMatchEnrollSet(){
        return "match_enroll_set";
    }

    /**
     * 模考大赛被导出考试数据的set
     * @return
     */
    public static String getMatchCountSet(){
        return "match_count_set";
    }

    /**
     *记录排名的考试的用户id数据set
     * @param paperId
     * @return
     */
    public static String getEstimateUserIdKey(int paperId) {
        return "estimate_user_id_"+paperId;
    }
}

