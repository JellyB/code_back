package com.huatu.tiku.request.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubjectiveQuestionReqV1  extends  UpdateQuestionReqV1{

    @NotNull(message = "选项内容不能为空")
    private String  stem;

    @NotBlank(message ="参考答案不能为空")
    private String answerComment;

    private  String analyzeQuestion;

    /**
     * 拓展信息
     */
    private String extend;

    /**
     * 总括要求
     */
    private String  omnibusRequirements;

}
