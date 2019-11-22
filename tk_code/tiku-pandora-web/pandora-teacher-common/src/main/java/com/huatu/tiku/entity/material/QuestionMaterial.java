package com.huatu.tiku.entity.material;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * 试题材料关系对象
 * Created by huangqp on 2018\4\14 0014.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "question_material")
public class QuestionMaterial extends BaseEntity {
    /**
     * 试题id
     */
    private Long questionId;
    /**
     * 材料id
     */
    private Long materialId;
    /**
     * 材料顺序
     */
    private Integer sort;

}
