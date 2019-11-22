package com.huatu.tiku.essay.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/7 19:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsUserVO {
    /**
     * 用户id
     */
    private Object id;
    /**
     * 用户昵称
     */
    private Object nick;
    /**
     * 用户手机
     */
    private Object mobile;
    /**
     * 用户地区
     */
    private Object area;
    /**
     * 分数
     */
    private double examScore;
    /**
     * 答题用时
     */
    private int spendTime;
}
