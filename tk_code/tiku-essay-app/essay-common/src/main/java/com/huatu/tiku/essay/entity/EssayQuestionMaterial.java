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
 * Created by x6 on 2017/11/26.
 * 试题-材料 关联表
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate(true)
@Table(name="v_essay_question_material")
@EqualsAndHashCode(callSuper = false)
public class EssayQuestionMaterial extends BaseEntity implements Serializable {

    /*  试题id（base表的id）  */
    private long questionBaseId;
    /*   材料id */
    private long materialId;
    /*   材料序号 */
    private int sort;
}
