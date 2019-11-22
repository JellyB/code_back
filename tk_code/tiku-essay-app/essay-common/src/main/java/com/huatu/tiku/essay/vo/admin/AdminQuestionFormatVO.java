package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;

/**
 * 格式规则
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@AllArgsConstructor
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminQuestionFormatVO {
    private long id;
    //格式类型，1代表只有标题；2代表有标题、称呼；3代表有标题、落款；4代表有标题、称呼和落款；5没有任何格式
    private Integer type;
    //试题id
    private long questionDetailId;
    //试卷id
    private long paperId;
    //无格式的分数
    private Double score;
    //标题格式规则信息
    private AnswerSubFormatVO titleInfo;
    //称呼格式规则信息
    private AnswerSubFormatVO appellationInfo;
    //落款格式规则信息
    private AnswerSubFormatVO inscribeInfo;

    public AdminQuestionFormatVO() {
        this.setTitleInfo(AnswerSubFormatVO.builder().score(0).childKeyWords(new LinkedList<>()).build());
        this.setAppellationInfo(AnswerSubFormatVO.builder().score(0).childKeyWords(new LinkedList<>()).build());
        this.setInscribeInfo(AnswerSubFormatVO.builder().score(0).childKeyWords(new LinkedList<>()).build());
    }

    public void myTrim() {
        if (this.getTitleInfo() != null) {
            this.getTitleInfo().myTrim();
        }
        if (this.getAppellationInfo() != null) {
            this.getAppellationInfo().myTrim();
        }
        if (this.getInscribeInfo() != null) {
            this.getInscribeInfo().myTrim();
        }
    }
}
