package com.huatu.tiku.essay.vo.edu;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayEduPaperVO {

    //id
    private long paperId;
    //地区id
    private long areaId;
    //答题限时（单位：秒）
    private int limitTime;
    //满分
    private double score;
    //名称
    private String name;
    //答题卡id
    private long answerCardId;
    //总题目数
    private int totalCount;
    //全站批改总次数
    private int correctSum;
    //用户该试卷批改次数
    private int correctNum;
    //用户最近一次答题的情况，0空白1未交卷2交卷未批改
    private int recentStatus;
    //用户做题次数
    private int times;

    private long modifyTime;

    private int status;

}
