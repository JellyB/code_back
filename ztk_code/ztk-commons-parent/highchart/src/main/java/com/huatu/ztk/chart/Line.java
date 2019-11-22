package com.huatu.ztk.chart;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * highcharts 曲线图,柱状图bean
 * Created by shaojieyue
 * Created time 2016-06-21 09:19
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Line implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> categories;//x轴标点
    private List<LineSeries> series;

}
