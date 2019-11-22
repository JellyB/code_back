package com.huatu.tiku.teacher.service.impl.match;

import com.huatu.ztk.question.bean.GenericQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
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