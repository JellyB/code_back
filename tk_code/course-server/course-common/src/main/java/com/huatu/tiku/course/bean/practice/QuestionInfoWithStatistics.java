package com.huatu.tiku.course.bean.practice;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-04-02 9:16 PM
 **/

@NoArgsConstructor
@Getter
@Setter
public class QuestionInfoWithStatistics implements Serializable{

    private QuestionInfo questionInfo;

    private List<Double> choiceRate;

    private Integer count;

    private double correctRate;


    @Builder
    public QuestionInfoWithStatistics(QuestionInfo questionInfo, List<Double> choiceRate, Integer count, double correctRate) {
        this.questionInfo = questionInfo;
        this.choiceRate = choiceRate;
        this.count = count;
        this.correctRate = correctRate;
    }
}
