package com.huatu.ztk.question.bean;

import lombok.Data;

/**
 * 单一主观题
 * Author: xuhuiqiang
 * Time: 2017-03-12  15:54 .
 */
@Data
public class GenericSubjectiveQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private String require; //题目要求
    private String scoreExplain;    //赋分说明
    private String referAnalysis;   //参考解析，作为参考答案
    private String answerRequire;   //答题要求
    private String examPoint;   //审题要求
    private String solvingIdea; //解题思路
    private String stem;    //题干
    private int parent; //父节点的id，若为0，表示为无父节点
    private int maxWordCount;   //最大字数限制
    private int minWordCount;   //最小字数限制
    private QuestionMeta meta;  //试题统计信息

    private String extend;  //试题拓展部分内容
}
