package com.huatu.ztk.backend.question.bean;


import lombok.*;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-10  10:51 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionPointTreeMin {
    private int id;//id
    private String name;//名称
    private int parent;//父节点
    private int level;//节点级别
    private int subject;//知识点所属科目
    private List<QuestionPointTreeMin> children;
}
