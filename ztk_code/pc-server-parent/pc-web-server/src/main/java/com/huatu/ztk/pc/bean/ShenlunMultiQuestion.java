package com.huatu.ztk.pc.bean;

import com.huatu.ztk.pc.common.ShenlunQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 申论复合题 table v_multi_question
 * Created by shaojieyue
 * Created time 2016-09-26 15:14
 */

@Data
@AllArgsConstructor
@Builder
public class ShenlunMultiQuestion extends ShenlunQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ShenlunSingleQuestion> subQuestions;//子题列表

    public ShenlunMultiQuestion() {
        this.setType(ShenlunQuestionType.MULTI_QUESTION);
    }
}
