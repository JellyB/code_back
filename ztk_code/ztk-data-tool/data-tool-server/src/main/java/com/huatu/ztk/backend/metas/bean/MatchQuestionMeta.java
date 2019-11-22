package com.huatu.ztk.backend.metas.bean;

import com.huatu.ztk.question.bean.GenericQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2017\11\15 0015.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchQuestionMeta {
    private GenericQuestion question;
    private int questionSeq;
    private long finishCount;
    private long wrongCount;
    private long rightCount;
    //错题率
    private double percent;
    private Map<String,Long> choiceTime;
}
