package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/29.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockQuestionAnswerVO {

    /**
     * 学员得分
     */
    private double examScore;
    /**
     * 单题分数
     */
    private double score;
    //总字数
    private int inputWordNum;

    //序号
    private int sort;
    //单题用时
    private int spendTime;
    //问题类型
    private int type;


}
