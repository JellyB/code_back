package com.huatu.ztk.pc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 申论按地区汇总
 * Created by shaojieyue
 * Created time 2016-09-26 15:36
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ShenlunSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int areaId;//地区id
    private String areaName;//地区名称
    private int count;//数量

}
