package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by x6 on 2017/12/26.
 * 微信预支付相关信息
 */
@Builder
@Entity
@Table(name="v_wx_pre_pay")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class WeChatPrePay {

    @Id
    @GeneratedValue
    private Integer id;

    //订单号
    private  Long  orderNum;

    //随机字符串
    private String nonceStr;

    //附加字符串
    private String attach;

    //商户订单号（用来获取预约号，唯一且不重复）格式为：20180710+订单后五位（不足用0补齐）
    private String outTradeNo;



}
