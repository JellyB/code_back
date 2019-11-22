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
public class  EssayStandardAnswerRuleWordNumVO {
    private long id;
    //规则类型，1为字数限制，2为句子冗余
    private int type;
    //二级规则，1为最多字数，2为最少字数，3为范围字数，4为单句最多字数
    private int  nextType;
    //最小字数
    private int minWordNum;
    //最大字数
    private int maxWordNum;
    //扣分规则，1为多字扣分，2为少字扣分，3为只扣分一次
    private int firstDeductType;
    //扣分规则中，字数要求，例如每多20字扣多少分
    private int firstDeductTypeWordNum;
    //扣分的分数
    private double firstDeductTypeScore;
    //第二个扣分规则，0为没有扣分规则，1为多字扣分，2为少字扣分，3为只扣分一次
    private int secondDeductType;
    //扣分规则中，字数要求，例如每多20字扣多少分
    private int secondDeductTypeWordNum;
    //扣分的分数
    private double secondDeductTypeScore;
    //关联的试题id
    private long questionDetailId;
}
