package com.huatu.ztk.paper.common;

/**
 * 试卷redis key 列表
 * Created by shaojieyue
 * Created time 2016-07-04 10:05
 */
public class PaperRedisKeys {

    public static final String getPaperKey(int paperId){
        return String.format("course_detail$%s", paperId);
    }

    /**
     * paper 练习id 分数 key
     * 用来存储试卷练习过的记录和分数,用于计算排名
     * @param paperId
     * @return
     */
    public static final String getPaperPracticeIdSore(int paperId){
        //paper_pi_score_${paperId}
        StringBuilder stringBuilder = new StringBuilder("paper_pi_score_").append(paperId);
        return stringBuilder.toString();
    }

    /**
     * 试卷,做题所有分数的和,用于计算分数的平均值
     * @param paperId
     * @return
     */
    public static final String getPaperScoreSum(int paperId){
        //paper_score_${paperId}
        StringBuilder stringBuilder = new StringBuilder("paper_score_").append(paperId);
        return stringBuilder.toString();
    }

    public static String getKeyWithType(String key,int type) {
        StringBuilder stringBuilder = new StringBuilder(key).append("_").append(type);
        return stringBuilder.toString();
    }

    /**
     * 根据试卷ID，答题卡类型统计答题卡ID，做答题卡数量统计
     * @return
     */
    public static String getPaperSubmitKey(int paperId,int answerCardType) {
        StringBuilder stringBuilder = new StringBuilder("paper_submit_").append(paperId).append("_").append(answerCardType);
        return stringBuilder.toString();
    }

    /**
     * 试卷提交时间记录zset
     * @param paperId
     * @param answerCardType
     * @return
     */
    public static String getPaperSubmitTimeKey(int paperId, int answerCardType) {
        StringBuilder stringBuilder = new StringBuilder("paper_submit_time_").append(paperId).append("_").append(answerCardType);
        return stringBuilder.toString();
    }
    
    /**
     * 获取录播随堂练统计信息存储hash key
     * @param courseId
     * @param courseType
     * @return
     */
    public static String getPracticeCourseIdAndCourseTypeKey(long courseId, int courseType) {
        StringBuilder stringBuilder = new StringBuilder("practice_submit_answer_sum").append(courseId).append("_").append(courseType);
        return stringBuilder.toString();
    }
}
