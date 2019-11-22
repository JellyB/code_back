package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * page 汇总接口
 * Created by shaojieyue
 * Created time 2016-07-01 10:56
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int area;//区域
    private String areaName;//区域名称
    private int paperCount;//试卷套数
}
