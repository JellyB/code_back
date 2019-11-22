package com.huatu.tiku.match.bo.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2019/1/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericAnswerAnalysisBo extends GenericQuestionAnalysisBo {

    /**
     * 在答题卡中对应的下标
     */
    private Integer answerCardIndex;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否正确
     * AnswerCardInfo.Result
     */
    private Integer correct;

    /**
     * 是否有疑问
     * 0 没有疑问  1 有疑问
     */
    private Integer doubt;

    /**
     * 耗时
     */
    private Integer expireTime;

}
