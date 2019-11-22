package com.huatu.ztk.knowledge.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 知识点树
 * Created by shaojieyue
 * Created time 2016-05-06 18:35
 */


@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionPointTree implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//id
    private String name;//名称
    @Getter(onMethod = @__({@JsonIgnore}))
    private int parent;//父节点
    private int qnum;//试题数
    private int rnum;//答对题数
    private int wnum;//答错题数
    private int unum;//未做题数
    private int times;//做题花费所有时间
    private int speed;//做题平均时间
    private int level;//节点级别
    private double accuracy;//正确率
    private List<QuestionPointTree> children;
    private long unfinishedPracticeId;//未完成的练习id
    private int userQnum;
}
