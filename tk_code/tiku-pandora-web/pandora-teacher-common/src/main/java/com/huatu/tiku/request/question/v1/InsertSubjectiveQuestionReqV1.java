package com.huatu.tiku.request.question.v1;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertSubjectiveQuestionReqV1 extends InsertQuestionReqV1 {

    @NotNull(message = "题干内容不能为空")
    private String  stem;

    @NotBlank(message ="参考答案不能为空")
    private String answerComment;

    private  String analyzeQuestion;

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
     *拓展信息
     */
    private String extend;

    /**
     *总括要求
     */
    private String omnibusRequirements;
}
