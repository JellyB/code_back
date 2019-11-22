package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by x6 on 2017/12/5.
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_user_answer_question_detailed_score")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayUserAnswerQuestionDetailedScore {

    @Id
    @GeneratedValue
    private long id;
   //类型
    private int type;
    //序号
    private int  sequenceNumber;
    //得分点
    private String scorePoint;
    //答题卡id
    private long  questionAnswerId;
    //分数
    private double score;
    // 1有效 0失效
    private int  status;

}
