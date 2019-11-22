package com.huatu.tiku.essay.vo.resp;

/**
 * Created by x6 on 2017/12/20.
 *
 *
 * 模考试卷批改  队列VO
 */
public class MockExamCorrectQueueVO {

    //答题卡
    private long answerCardId;

    //考试类型(0 真题  1模考)
    private int examType;

    //redis-key
    private String redisKey;


}
