package com.huatu.tiku.entity.duplicate;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by huangqp on 2018\5\16 0016.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "question_duplicate_subjective")
public class SubjectiveDuplicatePart extends BaseEntity {
    /**
     * 题型
     * 复用的数据只能在一种题型中使用，而且为了查询缩减范围，需要使用题型作为查询条件进行查询，
     * 而复用数据跟试题时一对多实现的，使用试题本身的题型多有不便，所以冗余一份题型在复用数据表中
     */
    private Integer questionType;
    /**
     * 题干
     */
    private String stem;
    private  String stemFilter;
    /**
     * 答案（参考答案）
     */
    private String answerComment;
    private  String answerCommentFilter;
    /**
     * 试题分析
     */
    private String analyzeQuestion;
    private String analyzeQuestionFilter;
    /**
     * 答题要求
     */
    private String answerRequest;
    private String answerRequestFilter;
    /**
     * 赋分说明
     */
    private String bestowPointExplain;
    private String bestowPointExplainFilter;

    /**
     * 解题思路
     */
    private String trainThought;
    private String trainThoughtFilter;

    /**
     * 总括要求
     */
    private String omnibusRequirements;
    private String omnibusRequirementsFilter;

    /**
     * 拓展
     */
    private String extend;
    private  String extendFilter;

}
