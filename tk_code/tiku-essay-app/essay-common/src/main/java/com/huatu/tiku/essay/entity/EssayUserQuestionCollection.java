package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/1/30.
 * 学员申论题目收藏关系表（试题，试卷）
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_user_question_collection")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayUserQuestionCollection extends BaseEntity implements Serializable {



    //用户id
    private int userId;

    //题目id
    private long questionBaseId;

    //题组id
    private long similarId;

    //题目类型
    private  int questionType;





}
