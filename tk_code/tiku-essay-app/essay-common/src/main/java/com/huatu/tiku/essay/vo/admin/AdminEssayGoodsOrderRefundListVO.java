package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 商品订单退款列表
 *
 * @author geek-s
 * @date 2019-07-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEssayGoodsOrderRefundListVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 申请时间
     */
    private Date gmtCreate;

    /**
     * 退款方式
     */
    private Integer refundType;

    /**
     * 实际支付金额
     */
    private Double realMoney;

    /**
     * 退款金额
     */
    private Double refundMoney;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人
     */
    private String modifier;

    /**
     * 用户名
     */
    private String name;

    /**
     * 商品订单ID
     */
    private Long goodsOrderId;
}
