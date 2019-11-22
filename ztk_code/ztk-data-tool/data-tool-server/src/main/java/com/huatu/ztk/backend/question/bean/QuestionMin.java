package com.huatu.ztk.backend.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-26  16:01 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionMin {
    private int id;//id
    private int type;//题型: 单选,多选,对错,复合题
    private String stem;//题干
    private int difficult;//难度系数
    private List<Integer> points;//知识点
    private List<String> pointsName;//知识点名称
    private String area;//地区名字
    private int moduleId;//模块id
    private int mode;//试题模式
    private int status;//试题状态
    private int channel;//试题添加途径
    private String createTime;//创建时间
}
