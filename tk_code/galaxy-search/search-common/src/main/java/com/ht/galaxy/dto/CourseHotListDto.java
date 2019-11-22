package com.ht.galaxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jbzm
 * @date Create on 2018/4/10 11:37
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseHotListDto {
    /**
     * 课程名称
     */
    private String title;
    /**
     * 课程销量
     */
    private int sales;
    /**
     * 销售金额
     */
    private int money;
    /**
     * 课程类型
     */
    private String courseType;
    /**
     * 日期
     */
    private String day;
}
