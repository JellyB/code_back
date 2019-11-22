package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.constant.status.EssayAnswerReductRuleConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/30
 * @描述
 */

public class EssaySubScoreRule {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum SubScoreEnum {
        wordLimit(EssayAnswerReductRuleConstant.WORDNUM_LIMIT, "字数限制"),

        sentenceTooLong(EssayAnswerReductRuleConstant.REDUNDANT_SENTENCE_LIMIT, "句子冗长"),

        subsection_rules(EssayAnswerReductRuleConstant.STRIP_SEGMENTAL_RANGE, "分段规则"),

        common_not_subsection(EssayAnswerReductRuleConstant.NORMAL_STRIP_RANGE, "普通分条(未分条扣分)"),

        common_subsection(EssayAnswerReductRuleConstant.SPECIAL_STRIP_RANGE, "普通分条(分条扣分)"),

        special_expression_not_exist(1, "特殊表达(不出现扣分)"),

        special_expression_exist(2, "特殊表达(出现扣分)");

        private int code;
        private String content;


    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum SubScoreTypeEnum {
        //1为少字扣分，2为最高分，3为直接扣分（分值），4为直接扣分（总分比例）
        max_score(2, "最高分"),
        direct_sub_score(3, "直接扣分(分数)"),
        getDirect_sub_score_percentage(4, "直接扣分(百分比%)");
        private int code;
        private String content;

    }

    public static SubScoreTypeEnum getSubScoreType(int code) {
        for (SubScoreTypeEnum subScoreTypeEnum : SubScoreTypeEnum.values()) {
            if (subScoreTypeEnum.code == code) {
                return subScoreTypeEnum;
            }
        }
        return SubScoreTypeEnum.direct_sub_score;
    }


}
