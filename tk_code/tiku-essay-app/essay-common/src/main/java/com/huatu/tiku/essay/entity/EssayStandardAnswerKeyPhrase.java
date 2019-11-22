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
@Table(name="v_essay_standard_answer_keyphrase")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayStandardAnswerKeyPhrase extends BaseEntity{

    private String item;
    //关键短句该出现的位置，1为出现在首段，2为出现在中间，3为出现在末尾4为全篇(默认2)
    private int  position;
    //关键短句分数
    private double score;
    //对应试题id
    private long questionDetailId;
    // 关键句类型：1为应用文关键句，2为议论文中心思想，3为议论文主题 4.关键句的描述 5.关键词的描述
    private int type;
    //上级id(上级id不为0。说明是关键句否则是近似句)
    private long pid;
    //TODO 论点划档暂时取消
//    //论点划档级别（1 一档 2 二档）
//    private int level;
}
