package com.huatu.tiku.course.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于物流列表的bean
 * Created by linkang on 12/6/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)  //去掉小写字母开头的字段
public class ExpressListItem {
    @JsonProperty("OrderNum")
    private String OrderNum;        //订单号

    @JsonProperty("ExpressNo")
    private String ExpressNo;       //运单号

    @JsonProperty("orderId")
    private String orderId;       //订单号

    @JsonProperty("ExpressCorp")
    private String ExpressCorp;     //物流公司

    @JsonProperty("Title")
    private String Title;           //物流标题

    @JsonProperty("scaleimg")
    private String scaleimg;        //图片url

    @JsonProperty("status")
    private int status;             //是否发货0和1
}
