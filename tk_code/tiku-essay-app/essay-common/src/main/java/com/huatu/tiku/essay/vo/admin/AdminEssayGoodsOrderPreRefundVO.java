package com.huatu.tiku.essay.vo.admin;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单预申请退款
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEssayGoodsOrderPreRefundVO {

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 支付类型(0 支付宝 1 微信 2 金币)
     */
    private Integer payType;

    /**
     * 应退款金额
     */
    private Double refundMoney;
}