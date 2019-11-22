package com.huatu.tiku.request.question.v1;

import com.huatu.tiku.request.material.MaterialReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\7\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertCommonQuestionReqV1 extends InsertQuestionReqV1 {
    /**
     * 判断依据(判断题)
     */
    private String judgeBasis;
    /**
     * 选项（选择题）
     */
    private List<String> choices;
    /**
     * 答案（判断，选择）
     */
    private String answer;
    /**
     * 题干（主观题，选择题，判断题）
     */
    private String stem;
    /**
     * 解析(选择题，判断题)
     */
    private String analysis;
    /**
     * 扩展信息（主观题，选择题，判断题）
     */
    private String extend;
    /**
     * 参考答案（主观题）
     */
    private String answerComment;
    /**
     * 试题分析（主观题）
     */
    private String analyzeQuestion;
    /**
     * 答题要求
     */
    private  String answerRequest;
    /**
     *赋分说明
     */
    private  String bestowPointExplain;
    /**
     * 解题思路
     */
    private  String trainThought;
    /**
     * 总括要求（复合题）
     */
    private String omnibusRequirements;
    /**
     * 材料（复合题）
     */
    private List<MaterialReq> materials;
}

