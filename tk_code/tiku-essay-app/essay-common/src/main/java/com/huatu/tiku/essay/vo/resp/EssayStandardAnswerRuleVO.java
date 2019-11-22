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
public class EssayStandardAnswerRuleVO {
    private long id;
    //规则类型，1为字数限制，2为句子冗余,3为分段规则，4为普通分条（不出现扣分），5为抄袭度匹配，6普通分条（出现扣分），7严重分条（不出现扣分），8严重分条（出现扣分）
    private int type;
    //最小字数或者最小段数，只有type为1，3的情况需要输入该列
    private int minNum;
    //最大字数或者最大段数，只有type为1,2,3的情况下需要输入该列
    private int maxNum;
    //扣分方式，1为少字扣分，2为最高分，3为直接扣分（分值），4为直接扣分（总分比例）
    private int deductType;
    //扣分分值或者百分比
    private double deductTypeScorePercent;
    //扣分参数，只有少字扣分需输入该列
    private int deductTypeNum;
    //关联的试题id
    private long questionDetailId;
    private int status = 1;
    private int bizStatus = 0;
}
