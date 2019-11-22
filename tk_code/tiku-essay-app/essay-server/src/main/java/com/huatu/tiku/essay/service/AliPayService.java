package com.huatu.tiku.essay.service;

import java.util.Map;

/**
 * 支付宝支付
 *
 * @author geek-s
 * @date 2019-07-15
 */
public interface AliPayService {

    /**
     * 校验回调签名
     *
     * @param reqData 参数
     * @return 签名是否正确
     * @throws Exception
     */
    Boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception;

    /**
     * 申请退款
     *
     * @param outTradeNo  交易号
     * @param refundMoney 退款金额
     * @param newPay 是否实用新支付宝 1是 0否
     */
    void refund(String outTradeNo, Integer refundMoney, Integer newPay);
}
