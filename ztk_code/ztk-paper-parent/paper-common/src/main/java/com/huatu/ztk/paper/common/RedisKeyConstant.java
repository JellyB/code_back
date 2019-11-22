package com.huatu.ztk.paper.common;


import com.google.common.base.Joiner;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2017-12-16 下午2:15
 **/
public class RedisKeyConstant {


    public static final String ESSAY_SERVER_NAME = "essay-server.";
    /*  永久缓存 修改后可手动删除   */
    /**
     * !!! key 中拼接1228是为了避免key重复 !!!
     */
    /*用户报名人数*/
    public static String MOCK_ENROLL_COUNT_PAPER="e_c_p";
    /*用户地区报名划分*/
    public static String MOCK_ENROLL_COUNT_PAPER_POSITION="e_c_p_p";


    /**
     * 申论端写入的缓存
     */
    /*  用户答题卡状态 （状态） 行测端使用*/
    public static String MOCK_USER_ANSWER_STATUS_PREFIX = "m_u_a_s_s_1228";






    /* 模考的总成绩Zset 无地区*/
    public static String MOCK_USER_TOTAL_SCORE_PREFIX = "t_u_s_p_1228";
    /* 模考的总成绩Zset 有地区*/
    public static String MOCK_USER_AREA_TOTAL_SCORE_PREFIX = "t_u_a_s_p_1228";
    /* 模考成绩求和 */
    public static String MOCK_SCORE_SUM_PREFIX = "m_s_s_1228";


    /**
     * 申论端读取的缓存
     */
    /* 用户参加模考的地区信息 （行测  提供）*/
    public static String MOCK_USER_AREA_PREFIX= "e_p_u_1228";

    /* 用户行测成绩的zSet*/
    public static String USER_PRACTICE_SCORE_PREFIX= "u_p_s_1228";
    /* 三方公用的userId的set （用户交卷时存入，批改完成后移除，行测端读取）*/
    public static String PUBLIC_USER_SET_PREFIX= "p_u_1228";






    /**
     * 三方共用的userId的set
     * @return
     */
    public static String  getPublicUserSetPrefix(long paperId) {
        return ESSAY_SERVER_NAME+ Joiner.on("_").join(RedisKeyConstant.PUBLIC_USER_SET_PREFIX,paperId);
    }


    /**
     * 用户答题卡状态(1未完成2已交卷3已批改)
     * @param paperId
     * @return
     */
    public static String  getUserAnswerStatusKey(long paperId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.MOCK_USER_ANSWER_STATUS_PREFIX,paperId);
    }




    /**
     * 用户参加模考的地区信息
     * @param paperId
     * @return
     */
    public static  String getMockUserAreaPrefix(long paperId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.  MOCK_USER_AREA_PREFIX,paperId);
    }



    /**
     *  用户模考总成绩zSet
     * @return
     */
    public static String  getMockUserTotalScoreKey(long paperId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.MOCK_USER_TOTAL_SCORE_PREFIX,paperId);
    }
    /**
     *  用户模考总成绩 地区zSet
     * @return
     */
    public static String  getMockUserAreaTotalScoreKey(long paperId,long areaId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.MOCK_USER_AREA_TOTAL_SCORE_PREFIX,paperId,areaId);
    }


    /**
     *  模考总成绩
     * @return
     */
    public static String  getMockScoreSumKey(long paperId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.MOCK_SCORE_SUM_PREFIX,paperId);
    }

    /**
     *  用户行测成绩 Zset(关联的essayPaperId做参数)
      * @return
     */
    public static String  getUserPracticeScoreKey(long paperId){
        return ESSAY_SERVER_NAME+Joiner.on("_").join(RedisKeyConstant.USER_PRACTICE_SCORE_PREFIX,paperId);
    }


    /**
     * 用户申论大赛报名人数
     * @param paperId
     * @return
     */
    public static String getTotalEnrollCountKey(long paperId) {
        return ESSAY_SERVER_NAME + Joiner.on("_").join(RedisKeyConstant.MOCK_ENROLL_COUNT_PAPER,paperId);
    }
    /**
     * 用户申论大赛报名人数
     * @param paperId
     * @param positionId
     * @return
     */
    public static String getPositionEnrollSetKey(long paperId,int positionId) {
        return ESSAY_SERVER_NAME + Joiner.on("_").join(RedisKeyConstant.MOCK_ENROLL_COUNT_PAPER_POSITION,paperId,positionId);
    }
    /**
     *  行测的信息
     * @return
     */
    public static String  getPracticeInfoKey(){
        return  ESSAY_SERVER_NAME+"practice_info";
    }
}
