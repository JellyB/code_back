package com.huatu.tiku.request.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertObjectiveQuestionReqV1 extends InsertQuestionReqV1 {
    /**
     * 选项
     */
    @NotNull(message = "选项内容不能为空")
    @NotEmpty(message = "选项内容不能为空")
    private List<String> choices;
    /**
     * 答案
     */
    @NotBlank(message = "答案不能为空")
    private String answer;
    /**
     * 题干
     */
    @NotBlank(message = "题干内容不能为空")
    private String stem;
    /**
     * 解析
     */
    @NotBlank(message = "解析不能为空")
    private String analysis;
    /**
     * 扩展信息
     */
    private String extend;

}
