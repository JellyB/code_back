package com.huatu.tiku.response.question.v1;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectSubjectiveQuestionRespV1 extends SelectQuestionRespV1 {


    /**
     * 题干
     */
    private String stem;
    /**
     * 答案（参考答案）
     */
    private String answerComment;
    /**
     * 试题分析
     */
    private String analyzeQuestion;
    /**
     * 答题要求
     */
    private String answerRequest;
    /**
     * 赋分说明
     */
    private String bestowPointExplain;
    /**
     * 解题思路
     */
    private String trainThought;

    /**
     *总括要求，只有复合材料题主题有，子题无
     */

    private String omnibusRequirements;
    /**
     * 拓展
     */
    private String extend;



}
