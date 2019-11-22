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
public class  EssayStandardAnswerRuleStripSegmentalVO {
    private long id;
    //分条分段规则类型，1为分段规则，2为严重分条
    private int type;
    //二级分段分条规则(只有1级规则类型选择分段规则时)，1为未分段，2为分段过多
    private int  nextType;
    //二级类型中分段过多时，最多分段数量，其他规则时默认为0
    private int maxParagraphNum;
    //扣分方式,1为直接扣分，2为最高分
    private int deductType;
    private double deductScore;
    //关联的试题id
    private long questionDetailId;
}
