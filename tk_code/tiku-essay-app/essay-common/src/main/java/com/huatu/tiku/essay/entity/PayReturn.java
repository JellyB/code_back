package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by x6 on 2017/11/29.
 */
@Builder
@Entity
@Table(name="v_pay_return")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class PayReturn {
    @Id
    @GeneratedValue
    private Integer id;

    private Date notifyTime;

    private String notifyType;

    private String notifyId;

    private String signType;

    private String sign;

    private String outTradeNo;

    private String subject;

    private String paymentType;

    private String tradeNo;

    private String tradeStatus;

    private String sellerId;

    private String sellerEmail;

    private String buyerId;

    private String buyerEmail;

    private Integer totalFee;

    private Integer quantity;

    private Integer price;

    private String body;

    private Date gmtCreate;

    private Date gmtPayment;

    private String isTotalFeeAdjust;

    private String useCoupon;

    private String discount;

    private String refundStatus;

    private Date gmtRefund;
    
    /**
     * 0 老支付宝
     * 1 新支付宝
     */
    private Integer newPay;

}
