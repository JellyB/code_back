package com.huatu.ztk.pc.bean;

import com.huatu.ztk.pc.common.ShenlunQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * 申论单个试题 table v_sub_question
 * Created by ht on 2016/9/23.
 */
@Data
@AllArgsConstructor
@Builder
public class ShenlunSingleQuestion extends ShenlunQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String restrict;//注意事项 answer_require
    private String answer;//答案 answer_comment
    private int wordLimit;//字数限制 input_word_num
    private String analysis;//解析 answer_think
    private String scorePoint;//得分点,评分标准 bestow_point_explain

    public ShenlunSingleQuestion() {
        this.setType(ShenlunQuestionType.SINGLE_QUESTION);
    }
}
