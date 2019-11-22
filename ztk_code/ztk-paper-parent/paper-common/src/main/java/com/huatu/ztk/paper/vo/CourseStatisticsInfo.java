package com.huatu.ztk.paper.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程正确率统计
 * @author shanjigang
 * @date 2019/3/13 14:39
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class CourseStatisticsInfo {
    /**
     * 正确率小于50
     */
    private int ltFifty;

    /**
     * 正确率大于50小于80
     */
    private int ltEighty;

    /**
     * 正确率大于80
     */
    private int gtEighty;

    /**
     * 测试平均正确率
     */
    private int averageAccuracy;

    /**
     * 交卷人数
     */
    private int submitNum=0;

    /**
     * 随堂练习正确率
     */
    private int accuracy;
}
