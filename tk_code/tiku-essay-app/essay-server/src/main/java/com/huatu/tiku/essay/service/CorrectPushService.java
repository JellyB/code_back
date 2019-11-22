package com.huatu.tiku.essay.service;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-02 4:12 PM
 **/
public interface CorrectPushService {

    /**
     * 答题卡被退回
     * @param answerCardId
     * @param answerCardType
     */
    void correctReturn4Push(long answerCardId, int answerCardType, String returnContent);

    /**
     * 答题卡被退回
     * @param answerCard
     * @param answerCardType
     * @param exercisesType
     * @param returnContent
     */
    void correctReturn4Push(Object answerCard, Integer answerCardType, Integer exercisesType, String returnContent);

    /**
     * 答题卡报告已出
     * @param answerCardId
     * @param answerCardType
     */
    void correctReport4Push(final long answerCardId, final int answerCardType);
}
