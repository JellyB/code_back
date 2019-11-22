package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2017/11/30.
 * 相似题 题组对象
 */
@Entity
@Data
@Builder
@Table(name="v_essay_similar_question_group")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssaySimilarQuestionGroupInfo extends BaseEntity  implements Serializable {

    //展示内容
    private String showMsg;
    /* 1归纳概括、 2 综合分析、3 提出对策、4 应用文、5 议论文 */
    private int type;
    /* 没有的话就默认是type */
    private int pType;









}
