package com.huatu.ztk.backend.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PracticeQuestion {
    private int id;//id
    private int type;//题型: 单选,多选,对错,复合题
    private String stem;//题干
    private int difficult;//难度系数
    private long createTime;//创建时间
    private int area;//地区
    private String areaName;//地区名称
    private int moduleId;//模块id
    private int subject;//科目id
    private int isContain;//该试题是否位于试卷中  1包含 0 不包含
}
