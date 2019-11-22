package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2017/12/19.
 *
 * 相似题组中的 题目
 */
@Entity
@Data
@Builder
@Table(name="v_essay_similar_question")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssaySimilarQuestion extends BaseEntity  implements Serializable {

    //单题组的id
    private long similarId;
    //题目id
    private long questionBaseId;


}
