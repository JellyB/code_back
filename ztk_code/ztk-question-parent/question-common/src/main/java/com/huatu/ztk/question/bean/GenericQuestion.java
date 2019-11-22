package com.huatu.ztk.question.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * 普通题bean,单选,多选,对错
 * Created by shaojieyue
 * Created time 2016-04-18 21:26
 */

@Data
public class GenericQuestion extends Question{
    private static final long serialVersionUID = 1L;

    private String stem;//题干
    private int answer;//标准答案
    private List<String> choices;//选项
    private String analysis;//解析
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int parent;
    @Getter(onMethod = @__({ @JsonIgnore }))
    private List<Integer> points;//知识点
    private List<String> pointsName;//知识点名称
    private QuestionMeta meta;//试题统计信息
    private int recommendedTime; //推荐用时
    //新加字段(拓展)
    private String extend;
}