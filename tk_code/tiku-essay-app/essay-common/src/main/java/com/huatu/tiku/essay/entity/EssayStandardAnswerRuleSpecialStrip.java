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
@Table(name="v_essay_standard_answer_rule_special_strip")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
/**
 * 特殊规则
 */
public class EssayStandardAnswerRuleSpecialStrip extends BaseEntity{
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
}
