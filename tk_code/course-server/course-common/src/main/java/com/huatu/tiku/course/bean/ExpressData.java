package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物流详情data bean
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ExpressData {
    private int status;     //运单状态
    private String com;     //快递公司名称
    private List<ExpressDetail> route;  //物流路由信息
}
