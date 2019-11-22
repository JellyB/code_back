package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/12.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSingleQuestionVO {

    /**
     * 试题基本id
     */
    private long id;
    private long questionBaseId;
    /**
     * 单题组和单题的关联id
     */
    private long relationId;
    /*地区id */
    private long areaId;
    /* 地区名称 */
    private String areaName;

    /*试题详情id */
    private long detailId;
    private long questionDetailId;

    /* 题序 */
    private int sort;

    /*  试题所属年份  */
    private String questionYear;

    /*  试题所属日期  */
    private String questionDate;

    /* 标题（展示信息） */
    private String title;

    /* 题干信息 */
    private String stem;

    private int bizStatus;
}
