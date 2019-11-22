package com.huatu.tiku.response.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectObjectiveQuestionRespV1 extends SelectQuestionRespV1 {
    /**
     * 选项
     */
    private List<String> choices;
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

}
