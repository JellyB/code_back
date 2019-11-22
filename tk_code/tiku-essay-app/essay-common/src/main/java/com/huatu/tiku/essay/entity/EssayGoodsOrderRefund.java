package com.huatu.tiku.essay.entity;

import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundDestEnum;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundTypeEnum;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品订单退款
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Entity
@Data
@Builder
@Table(name = "v_essay_goods_order_refund")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayGoodsOrderRefund extends BaseEntity implements Serializable {

    /**
     * 商品订单ID
     */
    private Long goodsOrderId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品订单明细ID
     */
    private Long goodsOrderDetailId;

    /**
     * 订单号
     */
    private String orderNumStr;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户ID
     */
    private String name;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 退款方式
     */
    private EssayGoodsOrderRefundTypeEnum refundType;

    /**
     * 实际支付金额
     */
    private Integer realMoney;

    /**
     * 退款金额
     */
    private Integer refundMoney;

    /**
     * 申请备注
     */
    private String remark;

    /**
     * 申请人ID
     */
    private Long creatorId;

    /**
     * 一级审批人
     */
    private Long operator1Id;

    /**
     * 一级审批人
     */
    private String operator1;

    /**
     * 一级审批备注
     */
    private String remark1;

    /**
     * 二级审批人
     */
    private Long operator2Id;

    /**
     * 二级审批人
     */
    private String operator2;

    /**
     * 二级审批备注
     */
    private String remark2;

    /**
     * 退款去向
     */
    private EssayGoodsOrderRefundDestEnum refundDest;
}
