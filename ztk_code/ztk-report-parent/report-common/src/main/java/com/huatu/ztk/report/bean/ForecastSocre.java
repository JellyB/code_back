package com.huatu.ztk.report.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 *
 * 用户预测分
 * 该预测分,每个用户,每天会有一条记录(只有当天做题的用户才有)
 * Created by shaojieyue
 * Created time 2016-06-20 15:27
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "forecast_socre")
public class ForecastSocre implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    private String id;//id
    private long uid;//用户id
    private int subject;//科目
    private double socre;//预测分
    private long forecastTime;//预测时间

}
