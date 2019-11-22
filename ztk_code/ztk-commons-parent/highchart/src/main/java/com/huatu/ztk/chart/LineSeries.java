package com.huatu.ztk.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 对应曲线图里面的Series的其中一条记录
 * Created by shaojieyue
 * Created time 2016-06-21 09:24
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class LineSeries implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<? extends Number> data;
    private List<? extends String> strData;

}
