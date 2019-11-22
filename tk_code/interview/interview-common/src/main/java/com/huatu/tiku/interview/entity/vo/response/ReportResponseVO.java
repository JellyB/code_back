package com.huatu.tiku.interview.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/1/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseVO {

    private long id;
    //第几天
    private Integer daySort;

    //统计日期
    private String reportDate;

    //总作答题目数
    private Integer totalAnswerCount;

    // 1自我认知  2工作实务   3策划组织  4综合分析   5材料题与特殊题型   6套题演练
    // 1自我认知  作答题目数
    private Integer oneAnswerCount;
    //2工作实务  作答题目数
    private Integer twoAnswerCount;
    //3策划组织  作答题目数
    private Integer threeAnswerCount;
    //4综合分析  作答题目数
    private Integer fourAnswerCount;
    //5材料题与特殊题型  作答题目数
    private Integer fiveAnswerCount;
    //6套题演练  作答题目数
    private Integer sixAnswerCount;

    //------------举止仪态-------------
    private Double behavior;
    //------------语言表达-------------
    private Double languageExpression;
    //------------是否精准扣题-------------
    private Double focusTopic;
    //------------是否条理清晰-------------
    private Double isOrganized;
    //------------是否言之有物-------------
    private Double haveSubstance;


    //------------报告类型（0 总计报告  1单日报告）-------------
    private Integer type;


    //------------举止仪态-------------
    private String behaviorAdvice;
    //------------语言表达-------------
    private String languageExpressionAdvice;
    //------------是否精准扣题-------------
    private String focusTopicAdvice;
    //------------是否条理清晰-------------
    private String isOrganizedAdvice;
    //------------是否言之有物-------------
    private String haveSubstanceAdvice;


    //------------老师评语-------------
    private List<String> remarkList;

}
