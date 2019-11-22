package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-23  16:03 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionForReview {
    private Question questionBefore;//变化前试题
    private QuestionExtend extendBefore;//变化前拓展
    private Question questionAfter;//变化后试题
    private QuestionExtend extendAfter;//变化后拓展
    private MarkModify markModify;//标记变化位置
    private String applierName;//修改者姓名
    private long applyTime;//修改时间
    private String areaName;//地区名字
    private float scoreBefore;
    private float scoreAfter;
    private int status;
    private int opType;//操作类型，0为不通过，1为通过
    private List<String> modifyAttribute;//修改的属性
    private List<QuestionForReview> questionReviewList;
}
