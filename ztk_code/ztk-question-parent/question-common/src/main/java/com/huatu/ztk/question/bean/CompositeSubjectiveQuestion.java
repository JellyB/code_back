package com.huatu.ztk.question.bean;

import lombok.Data;

import java.util.List;

/**
 * 复合主观题
 * Author: xuhuiqiang
 * Time: 2017-03-12  15:55 .
 */
@Data
public class CompositeSubjectiveQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private List<Integer> questions;//子题id
    private String require;//题目要求
}
