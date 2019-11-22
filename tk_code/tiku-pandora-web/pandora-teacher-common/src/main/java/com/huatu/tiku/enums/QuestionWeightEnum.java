package com.huatu.tiku.enums;

import com.huatu.tiku.constants.QuestionWeightConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;


public enum QuestionWeightEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum WeightEnum{
        YEAR("试卷年分", QuestionWeightConstant.YEAR_PERCENT, QuestionWeightConstant.YEAR_BOOLEAN),
        WRONG_PERCENT("错误率", QuestionWeightConstant.WRONG_PERCENT, QuestionWeightConstant.WRONG_BOOLEAN),
        MODE("真题/模拟题", QuestionWeightConstant.MODE_PERCENT, QuestionWeightConstant.MODE_BOOLEAN),
        DO_NUM("做题次数", QuestionWeightConstant.DO_NUM_PERCENT, QuestionWeightConstant.DO_NUM_BOOLEAN);

        private String name;
        private Function<Integer,Double> percentEnum;
        private Function<Integer,Boolean> flag;
    }



}
