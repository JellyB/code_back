package com.huatu.tiku.essay.vo.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Created by x6 on 2017/12/4.
 * 生成订单响应数据
 */
@Builder
@Data
public class OrderResponseVO {

    /* ======支付宝支付====== */
    //订单编号
    private String orderNum;
    private String orderNumStr;//订单号
    //总金额
    private String moneySum;
    //订单描述
    private String description;
    //商品名称
    private String title;
    //回调地址
    private String notifyUrl;
    //客户端支付字符串
    private String orderStr;


    /*  ======微信支付====== */
    //appId
    private String appId;
    //随机字符串
    public String nonceStr;
    public String packageValue;
    public String partnerId;
    public String prepayId;
    public String sign;
    public String timestamp;
    /** 回调验证key */
    public String returnKey;
}
