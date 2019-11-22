package com.huatu.tiku.interview.entity.po;

/**
 * Created by x6 on 2018/4/10.
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_question_info")
public class QuestionInfo  extends BaseEntity{
    //题干信息
    private String stem;
    //试卷id
    private long paperId;
    //题目类型(1单选 2多选 3排序 4简答)
    private int questionType;



}
