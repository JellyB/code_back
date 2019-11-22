package com.huatu.tiku.essay.service;

import java.util.Map;

/**
 * 微信支付
 *
 * @author geek-s
 * @date 2019-05-07
 */
public interface WxPayService {

    /**
     * 校验回调签名
     *
     * @param reqData 参数
     * @return 签名是否正确
     * @throws Exception
     */
    Boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception;

    /**
     * 退款
     *
     * @param outRefundNo   退款单号
     * @param transactionId 交易号
     * @param refundFee     退款金额
     * @param totalFee      总金额
     */
    void refund(String outRefundNo, String transactionId, Integer refundFee, Integer totalFee);
}
