package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 减分规则
 * Created by huangqp on 2017\12\8 0008.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionDeductRuleVO {
    private long questionDetailId;
    /**
     * 普通扣分规则
     */
    private List<EssayStandardAnswerRuleVO> commonRuleList;
    /**
     * 特殊表达规则
     */
    private List<EssayStandardAnswerRuleSpecialStripVO> specialStripList;
}
