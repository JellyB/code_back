package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Entity
@Data
@Builder
@Table(name="v_essay_standard_answer_format")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayStandardAnswerFormat extends BaseEntity implements Serializable{
    //格式类型，1代表只有标题；2代表有标题、称呼；3代表有标题、落款；4代表有标题、称呼和落款；5没有任何格式
    private int type;
    //标题分数
    private double titleScore;
    //称呼分数
    private double appellationScore;
    //落款
    private double inscribeScore;
    //对应试题
    private long questionDetailId;
}
