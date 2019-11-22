package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 套题批改报告题目VO
 * @date 2018/12/67:54 PM
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperReportQuestionVO {
    //题目id
    private long questionBaseId;
    //题目序号
    private int sort;
    //题目类型
    private int type;
    //题目类型名称
    private String typeName;
    //题目得分
    private double examScore;
    //题目满分
    private double score;
    //题目用时
    private int spendTime;
    //答题字数
    private int inputWordNum;
}
