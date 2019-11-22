package com.huatu.tiku.request.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJudgeQuestionReqV1 extends UpdateQuestionReqV1 {
    /**
     * 判断依据
     */
    private String judgeBasis;
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
