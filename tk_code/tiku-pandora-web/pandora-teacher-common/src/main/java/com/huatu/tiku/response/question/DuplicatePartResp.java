package com.huatu.tiku.response.question;

import lombok.Data;

import java.util.List;

/**
 * 所有去重数据
 * Created by huangqp on 2018\5\17 0017.
 */
@Data
public class DuplicatePartResp {
    /**
     * 使用该复用数据的试题id
     */
    private Long questionId;
    /**
     * 复用数据的id
     */
    private Long duplicateId;
    /**
     * 题干
     */
    private String stem;
    private String stemShow;
    /**
     * 答案
     */
    private String answer;
    private String answerShow;
    /**
     * 解析
     */
    private String analysis;
    private String analysisShow;
    /**
     * 扩展
     */
    private String extend;
    private String extendShow;
    /**
     * 选项
     */
    private List<String> choices;
    private List<String> choicesShow;
    /**
     * 参考答案
     */
    String answerComment;
    String answerCommentShow;
    /**
     * 试题分析
     */
    String analyzeQuestion;
    String analyzeQuestionShow;
    /**
     * 答题要求
     */
    String answerRequest;
    String answerRequestShow;
    /**
     * 赋分说明
     */
    String bestowPointExplain;
    String bestowPointExplainShow;
    /**
     * 解题思路
     */
    String trainThought;
    String trainThoughtShow;
    /**
     * 总括要求
     */
    String omnibusRequirements;
    String omnibusRequirementsShow;

    /**
     * 科目ID
     */
    private int subjectId;

    /**
     * 试题类型(为前端判断)
     */
    private int questionType;
    /**
     * DB中存储的字符串
     */
    private String choicesStr;

    /**
     * 展示的科目
     */
    private String subjectShow;
    /**
     * 父节点
     */
    private String parent;
    /**
     *
     */
    private String subjectName;
}
