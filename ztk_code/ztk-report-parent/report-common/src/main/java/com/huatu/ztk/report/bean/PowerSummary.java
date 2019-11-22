package com.huatu.ztk.report.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 能力概述
 * Created by shaojieyue
 * Created time 2016-05-27 09:54
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PowerSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private long uid;//用户id
    private int subject;//科目
    private double score;//分数
    private double avg;//平均分
    private double beat;//击败的比例
    private int rank;//排名
}
