package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jbzm on 2018年1月4日17:48:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EssaySimilarQuestionVO {
    /**
     * 单题id
     */
    private Long id;
    /**
     * 试题所属年份   question_base 表
     */
    private String questionYear;
    /**
     * 地区名称
     */
    private String areaName;
    /**
     * 试题分数     question_detail 表
     */
    private Double score;
    /**
     * 题干 (单题的题目有，套题没有)
     */
    private String stem;

}
