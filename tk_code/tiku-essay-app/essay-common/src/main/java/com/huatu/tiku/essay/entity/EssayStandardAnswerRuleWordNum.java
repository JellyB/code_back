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
@Table(name="v_essay_standard_answer_rule_word_num")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
/**
 * 字数规则
 */
public class EssayStandardAnswerRuleWordNum extends BaseEntity{
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
