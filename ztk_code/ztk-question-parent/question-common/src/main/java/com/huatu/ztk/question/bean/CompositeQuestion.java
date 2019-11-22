package com.huatu.ztk.question.bean;

import com.huatu.ztk.question.common.QuestionType;
import lombok.Data;

import java.util.List;

/**
 * 复合题bean
 * Created by shaojieyue
 * Created time 2016-04-18 21:24
 */

@Data
public class CompositeQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private List<Integer> questions;//复合题类型
    public CompositeQuestion() {
        this.setType(QuestionType.COMPOSITED);
    }

    public List<Integer> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Integer> questions) {
        this.questions = questions;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CompositeQuestion{");
        sb.append("questions=").append(questions);
        sb.append('}');
        return sb.toString();
    }
}
