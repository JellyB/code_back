package com.huatu.tiku.essay.vo.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 扣分规则元素数据整合表（包含普通扣分和特殊规则需要的属性）
 * Created by huangqp on 2017\12\12 0012.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminCommonDeductVO {
    //规则类型，1为字数限制，2为句子冗余,3为分段规则，4为严重分条,5为特殊分条
    private String type;
    //最小字数或者最小段数，只有type为1，3的情况需要输入该列
    private int minNum;
    //最大字数或者最大段数，只有type为1,2,3的情况下需要输入该列
    private int maxNum;
    //扣分方式，1为少字扣分，2为最高分，3为直接扣分（分值），4为直接扣分（总分比例）
    private int deductType;
    //扣分分值或者百分比
    private double  deductTypeScorePercent;
    //扣分参数，只有少字扣分需输入该列
    private int deductTypeNum;
    //特殊分条的分条关键词组
    private List<String> specialWords;
    private long questionDetailId;
    private long id;

    public void myTrim() {
        if(CollectionUtils.isNotEmpty(this.getSpecialWords())){
            for(String str : this.getSpecialWords()){
                str = str.trim();
            }
        }
    }
}
