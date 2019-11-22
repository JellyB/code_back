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
@Table(name="v_essay_standard_answer_rule_strip_segmental")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
/**
 * 分段，分条规则
 */
public class EssayStandardAnswerRuleStripSegmental extends BaseEntity{
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
