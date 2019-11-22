package com.huatu.tiku.course.bean.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题统计信息
 * Created by lijun on 2019/2/18
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class QuestionMetaBo {

    /**
     * ID
     */
    private Long id;

    /**
     * 做题次数
     */
    private int count;

    /**
     * 用户选择答案列表
     */
    private int[] percents;

    /**
     * 正确答案序号
     */
    private String answer;

    /**
     * 平均答题用时
     */
    private Integer avgTime;

    /**
     * 正确率 - 需要考虑多选
     */
    private Double correctCate;

    /**
     * 剩余练习时间
     */
    private Integer lastPracticeTime;
    
    /**
     * 试题类型
     */
    private Integer type;

}
