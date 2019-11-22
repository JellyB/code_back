package com.huatu.tiku.course.bean.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-31 5:30 PM
 **/


@Getter
@Setter
@NoArgsConstructor
public class EssayCourseWorkSyllabusInfo implements Serializable{

    private Long questionId;

    private Long paperId;

    private Integer questionType;

    private Long similarId;

    private String areaName;

    private String paperName;

    private String questionName;

    private Long answerCardId;

    private Integer bizStatus;

    /* 退回原因（退回学员原因,其他类型原因均在操作日志表中查询）*/
    private String correctMemo;

    private String clickContent;

    private Integer afterCoreseNum;
    // 0 单题 1 套题
    private Integer buildType;
    // 完成数量
    private int fcount;


    @Builder

    public EssayCourseWorkSyllabusInfo(Long questionId, Long paperId, Integer questionType, Long similarId, String areaName, String paperName, String questionName, Long answerCardId, Integer bizStatus, String correctMemo, String clickContent, Integer afterCoreseNum, Integer buildType) {
        this.questionId = questionId;
        this.paperId = paperId;
        this.questionType = questionType;
        this.similarId = similarId;
        this.areaName = areaName;
        this.paperName = paperName;
        this.questionName = questionName;
        this.answerCardId = answerCardId;
        this.bizStatus = bizStatus;
        this.correctMemo = correctMemo;
        this.clickContent = clickContent;
        this.afterCoreseNum = afterCoreseNum;
        this.buildType = buildType;
    }
}
