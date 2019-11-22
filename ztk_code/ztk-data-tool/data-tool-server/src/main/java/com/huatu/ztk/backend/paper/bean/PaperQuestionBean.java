package com.huatu.ztk.backend.paper.bean;

import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linkang on 3/10/17.
 */


@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperQuestionBean {
    private int tikuType;
    private Question question;
    private float score;
    private QuestionExtend extend;
    private List<PaperQuestionBean> childrens;
    private int index;  //题目下标
}
