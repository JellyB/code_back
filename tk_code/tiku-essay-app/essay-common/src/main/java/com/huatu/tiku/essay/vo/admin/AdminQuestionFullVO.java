package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2017\12\19 0019.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AdminQuestionFullVO {
    public AdminQuestionVO singleQuestion;
    public AdminQuestionFormatVO format;
    public AdminQuestionKeyRuleVO keyRule;
    public AdminQuestionDeductRuleVO deductRules;
}
