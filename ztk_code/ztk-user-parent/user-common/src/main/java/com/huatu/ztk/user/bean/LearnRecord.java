package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-25  17:28 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class LearnRecord {
    private String name;//名称（课程名称、试卷名称）
    private int type;//学习记录类型
    private long endTime;//最后一次学习时间
}
