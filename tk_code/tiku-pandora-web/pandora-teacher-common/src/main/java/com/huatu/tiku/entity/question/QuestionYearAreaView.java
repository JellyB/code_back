package com.huatu.tiku.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by lijun on 2018/8/24
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "view_question_year_area")
public class QuestionYearAreaView {

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

}
