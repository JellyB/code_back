package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by huangqp on 2017\11\21 0021.
 * 试题材料表
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_material")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayMaterial  extends BaseEntity{
    //资料
    private String content;
    private long paperId;
    private int sort;
    //@ManyToMany(mappedBy = "essayMaterialList" )
    //private List<EssayQuestionDetail> essayQuestionList;
}
