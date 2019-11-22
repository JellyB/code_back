package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/11/27.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCreateVO {
    private List<OrderGoodsVO> goods;
    private Integer total;
    private Integer payType;
    private Long orderId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户名
     */
    private String name;
    /**
     * 是否实用新支付宝账户支付1是0否
     */
    private Integer newPay = 0;
}
