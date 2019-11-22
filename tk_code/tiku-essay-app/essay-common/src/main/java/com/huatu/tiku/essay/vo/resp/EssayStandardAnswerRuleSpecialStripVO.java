package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayStandardAnswerRuleSpecialStripVO {
    private long id;
    //规则分类，1为不出现扣分2，为出现扣分
    private int type;
    //扣分方式，1为直接扣分，2为最高分
    private int deductType;
    //扣分方式的分数
    private double  deductScore;
    //特殊规则中的词语个数，例如特殊规则“以……方面”，包含“以”、“方面”两个词
    private int wordNum;
    private String firstWord;
    private String secondWord;
    private String thirdWord;
    private String fourthWord;
    private String fifthWord;
    //关联的试题id
    private long questionDetailId;
    private int status = 1;
    private int bizStatus = 0 ;
}
