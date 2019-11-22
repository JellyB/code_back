package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-02  15:13 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionFull {
    private Question question;
    private QuestionExtend questionExtend;
    private List<QuestionFull> subQuestions;
}
