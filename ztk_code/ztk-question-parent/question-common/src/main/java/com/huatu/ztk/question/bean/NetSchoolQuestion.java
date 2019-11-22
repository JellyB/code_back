package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网校试题bean
 * json不忽略points，score，parent
 * Created by linkang on 8/29/16.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class NetSchoolQuestion extends Question{
    private static final long serialVersionUID = 1L;

    private String stem;//题干
    private int answer;//标准答案
    private List<String> choices;//选项
    private String analysis;//解析
    private float score;//分数
    private int difficult;//难度系数
    private int parent;
    private List<Integer> points;//知识点
    private List<String> pointsName;//知识点名称
}
