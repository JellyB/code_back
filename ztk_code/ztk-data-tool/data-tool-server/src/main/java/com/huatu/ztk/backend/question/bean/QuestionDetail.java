package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-03-16  00:56 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionDetail {
    private Question question;
    private QuestionExtend questionExtend;
    private List<QuestionDetail> subQuestions;//子题
    private Paper paper;//试题所属的试卷信息
    private float score;
    private int status;
}
