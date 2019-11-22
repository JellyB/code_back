package com.huatu.tiku.essay.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/6 22:50
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsSingleGradeVO {
    /**
     * 用户名
     */
    private String user;
    /**
     * 手机号
     */
    private Integer mobilePhone;
    /**
     * 用户地区
     */
    private String userArea;
    /**
     * 分数
     */
    private Double score;
    /**
     * 答题用时
     */
    private Integer spendTime;
}
