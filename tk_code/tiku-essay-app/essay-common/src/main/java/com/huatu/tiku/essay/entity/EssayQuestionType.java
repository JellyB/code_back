package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2017/11/24.
 * 题目类型表
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_question_type")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayQuestionType extends BaseEntity  implements Serializable {

    //题目类型名称
    private String name;

    //优先级
    private int sort;

    //上级id
    private long pid;

}
