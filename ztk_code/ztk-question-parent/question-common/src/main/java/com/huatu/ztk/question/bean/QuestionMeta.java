package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * 试题meta信息
 * Created by shaojieyue
 * Created time 2016-09-02 17:23
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int avgTime;//平均时间
    private int count;//做题次数
    private int[] answers;//用户选择答案列表
    private int[] counts;//用户选择答案次数 其和answers的index一一对应
    private int[] percents;//正确率
    private int yc;//易错项，可选
    private int rindex;//正确答案索引位置
}