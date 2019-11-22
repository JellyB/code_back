package com.huatu.tiku.match.bo.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2019/2/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericSubjectiveQuestionAnalysisBo extends GenericSubjectiveQuestionSimpleBo{

    /**
     * 在答题卡中对应的下标
     */
    private Integer answerCardIndex;
    /**
     * 赋分说明
     */
    private String scoreExplain;    //赋分说明

    /**
     * 参考解析，作为参考答案
     */
    private String referAnalysis;

    /**
     * 审题要求
     */
    private String examPoint;

    /**
     * 解题思路
     */
    private String solvingIdea;

    /**
     * 拓展
     */
    private String extend;

}
