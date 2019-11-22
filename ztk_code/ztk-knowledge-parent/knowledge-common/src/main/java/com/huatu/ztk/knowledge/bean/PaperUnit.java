package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-12  17:28 .
 * 遗传算法中种群内的个体
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//表示试卷种群中第几个paper
    private double year;
    private double difficulty;
    private int qNum;
    private List<QuestionGeneticBean> questions;
    private double moduleCoverage;//模块覆盖率
    private double adaptationDegree;//适应度
}
