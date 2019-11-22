package com.huatu.tiku.response.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectJudgeQuestionRespV1 extends SelectQuestionRespV1{
    /**
     * 判断依据
     */
    private String judgeBasis;
    /**
     * 答案
     */
    private String answer;
    /**
     * 题干
     */
    private String stem;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 扩展信息
     */
    private String extend;
    /**
     * 答案展示
     */
    private String answerDetail;

}
