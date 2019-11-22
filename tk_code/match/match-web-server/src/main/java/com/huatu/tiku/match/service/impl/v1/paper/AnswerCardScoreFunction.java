package com.huatu.tiku.match.service.impl.v1.paper;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * 定义分数计算规则
 * Created by lijun on 2019/1/4
 */
final class AnswerCardScoreFunction {

    /**
     * 试题平均值计算
     */
    private final static Function<AnswerCard, Double> questionAverage = (answerCard) -> {
        if (null == answerCard) {
            return NumberUtils.DOUBLE_ZERO;
        }
        if (answerCard.getRcount() == NumberUtils.INTEGER_ZERO) {
            return NumberUtils.DOUBLE_ZERO;
        }
        int total = 100;
        if(answerCard instanceof StandardCard){
            total = ((StandardCard) answerCard).getPaper().getScore();
        }
        BigDecimal source = new BigDecimal((double) answerCard.getRcount() / (double) answerCard.getCorrects().length * total).setScale(2, BigDecimal.ROUND_HALF_UP);
        return source.doubleValue();
    };

    private final static Function<AnswerCard,Double> questionScoreSum = (answerCard -> {
        if (null == answerCard) {
            return NumberUtils.DOUBLE_ZERO;
        }
        if(answerCard instanceof StandardCard){
            Paper paper = ((StandardCard) answerCard).getPaper();
            List<Double> scores = paper.getScores();
            int[] corrects = answerCard.getCorrects();
            if(corrects.length == scores.size()){
                Double score = 0D;
                for (int i = 0; i< corrects.length;i++){
                    if(corrects[i] == 1){
                        score += scores.get(i);
                    }
                }
                return score;
            }
        }
        return NumberUtils.DOUBLE_ZERO;

    });
    public static Function<AnswerCard, Double> getQuestionAverage() {
        return questionAverage;
    }

    public static Function<AnswerCard,Double> getQuestionScoreSum(){
        return questionScoreSum;
    }
}
