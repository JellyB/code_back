package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_question_answer")
public class QuestionAnswer extends BaseEntity{

    //答案信息（单选多选提的话是id1,id2）
    private String content;
    //试卷id
    private long questionId;
    //用户Id
    private String openId;

    private long mockId;
    //课堂互动pushId
    private Long pushId;

}
