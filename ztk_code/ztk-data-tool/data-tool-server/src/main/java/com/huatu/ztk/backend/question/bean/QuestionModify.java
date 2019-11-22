package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.question.bean.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-20  16:33 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionModify {
    private int id;//id
    private int qid;//要纠错的试题id
    private long uid;//申请人id
    private String content;//申请内容
    private String uname; //申请人name
    private long reviewerId;//审核人id
    private String reviewerName; //审核人name
    private long createTime; //创建时间
    private long reviewTime; //审核时间
    private int status;//编辑记录的状态，-1为未审核，0为未通过（或撤销），1为通过
    private int subject;//题所属类目
    private int type;//题型
    private int module;//模块
    private String areaName;
    private String subSign;
    private int paperId;
    private Question question;
    private String reviewContent; //审核内容

    private PaperQuestionBean paperQuestionBean;
}
