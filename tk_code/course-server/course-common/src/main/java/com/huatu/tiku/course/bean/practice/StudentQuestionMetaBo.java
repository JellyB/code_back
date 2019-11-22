package com.huatu.tiku.course.bean.practice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2019/2/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuestionMetaBo extends QuestionMetaBo{

    /**
     * 用户答案
     */
    private String userAnswer;

    /***
     * 用时
     */
    private Integer time;

    /**
     * 已测试题总量
     */
    private Integer totalQuestionNum;

    /**
     * 用户已答试题数量
     */
    private Integer userQuestionNum;

    /**
     * 正确试题数量
     */
    private Integer rightQuestionNum;

}
