package com.huatu.tiku.essay.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商品订单
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
public class AdminEssayGoodsOrderRefundDto {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long goodsOrderId;

    /**
     * 订单明细ID
     */
    private Long goodsOrderDetailId;

    /**
     * 退款方式
     */
    @NotNull(message = "退款方式不能为空")
    private Integer refundType;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    private Double refundMoney;

    /**
     * 备注
     */
    private String remark;

    /**
     * 退款去向
     */
    @NotNull(message = "退款去向不能为空")
    private Integer refundDest;
}
