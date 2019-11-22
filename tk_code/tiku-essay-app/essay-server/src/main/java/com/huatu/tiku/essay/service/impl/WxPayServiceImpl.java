package com.huatu.tiku.essay.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.status.SystemConstant;
import com.huatu.tiku.essay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final WXPay wxPay;

    public WxPayServiceImpl() {
        wxPay = new WXPay(new WXPayConfig() {

            @Override
            public String getAppID() {
                return SystemConstant.APP_ID;
            }

            @Override
            public String getMchID() {
                return SystemConstant.MCH_ID;
            }

            @Override
            public String getKey() {
                return SystemConstant.PARTNER_KEY;
            }

            @Override
            public InputStream getCertStream() {
                return WxPayServiceImpl.class.getResourceAsStream("/wx_apiclient_cert.p12");
            }

            @Override
            public int getHttpConnectTimeoutMs() {
                return 6000;
            }

            @Override
            public int getHttpReadTimeoutMs() {
                return 8000;
            }
        }, WXPayConstants.SignType.MD5);
    }

    @Override
    public Boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception {
        return wxPay.isPayResultNotifySignatureValid(reqData);
    }

    @Override
    public void refund(String outRefundNo, String transactionId, Integer refundFee, Integer totalFee) {
        Map<String, String> params = Maps.newHashMap();

        params.put("transaction_id", transactionId);
        params.put("out_refund_no", outRefundNo);
        params.put("refund_fee", refundFee.toString());
        params.put("total_fee", totalFee.toString());

        try {
            Map<String, String> response = wxPay.refund(params);

            log.info("{} `s Refund response is {}", transactionId, response);

            Assert.isTrue("SUCCESS".equals(response.get("result_code")), "微信申请退款异常");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
