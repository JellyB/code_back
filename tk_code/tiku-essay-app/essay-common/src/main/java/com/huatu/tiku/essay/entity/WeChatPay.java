package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by x6 on 2017/12/1.
 */
@Builder
@Entity
@Table(name="v_wx_pay")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class WeChatPay {
    @Id
    @GeneratedValue
    private Integer id;

    private String appId;

    private String feeType;

    private String isSubscribe;

    private String nonceStr;

    private String outTradeNo;

    private String transactionId;

    private String tradeType;

    private String resultCode;

    private String sign;

    private String mchId;

    private String totalFee;

    private String timeEnd;

    private String openid;

    private String bankType;

    private String returnCode;

    private String couponFee;

    private String couponCount;

    private String cashFee;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    protected Date gmtCreate;

    /**
     * 订单ID
     */
    private Long orderId;
}
