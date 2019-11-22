package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Entity
@Data
@Builder
@Table(name="v_essay_standard_answer_rule")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayStandardAnswerRule extends BaseEntity{
    //规则类型，1为字数限制，2为句子冗余,3为分段规则，4为严重分条,5为抄袭度匹配
    private int type;
    //最小字数或者最小段数，只有type为1，3的情况需要输入该列
    private int minNum;
    //最大字数或者最大段数，只有type为1,2,3,5的情况下需要输入该列
    private int maxNum;
    //扣分方式，1为少字扣分，2为最高分，3为直接扣分（分值），4为直接扣分（总分比例）
    private int deductType;
    //扣分分值或者百分比
    private double  deductTypeScorePercent;
    //扣分参数，只有少字扣分需输入该列
    private int deductTypeNum;
    //关联的试题id
    private long questionDetailId;
}
