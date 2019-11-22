
package com.huatu.tiku.teacher.service.impl.paper;

import org.apache.commons.collections.CollectionUtils;
import java.math.BigDecimal;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/27
 * @描述
 */
public class PaperQuestionScoreHandler {


    /**
     * 计算试卷试题总分
     *
     * @param scores
     * @return
     */
    public static Double getQuestionTotalScore(List<Double> scores) {
        Double questionTotalScore = 0D;
        if (CollectionUtils.isNotEmpty(scores)) {
            questionTotalScore = scores.stream().map(score -> {
                BigDecimal bigDecimalScore = new BigDecimal(score);
                return bigDecimalScore;
            }).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
        }
        return questionTotalScore;
    }
}