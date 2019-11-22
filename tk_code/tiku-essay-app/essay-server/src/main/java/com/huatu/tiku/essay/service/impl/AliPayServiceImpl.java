package com.huatu.tiku.essay.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.service.AliPayService;
import com.huatu.tiku.essay.util.pay.AliPayConfig;
import com.huatu.tiku.essay.util.pay.AliPayConfigNew;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AliPayServiceImpl implements AliPayService {

    private String APP_ID = AliPayConfig.app_id;
    private String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7iVeHIgtLnazNjurEsZ88H1gwqaNOalStQvJhr/+byLea5BU7YdAa4ti13oXWASb01wvqPHcE7kU6fcel40pSwa4CfQTk12VhwXf8Oq1KYBjG1WDRiVEj8cs4P5vcRKJWYDypZd2idD8ycNffsaHv7gacHCLIry28d1I78HHjm/esgqGLCy6feCryYVBr9ESM5qN11Lv4BSjxMSu+tQ0vwjCxgAdG3TJvutLLAyki750FlNHmL9k3oC76gzRDWqUbrrykLCeGwvWgrkHMibJ/yix81o9vyRVzE/JgmANNOtxuKIUtfOXSYE3HsnTjSHYucslmqleEP9w8vk+CsqMHAgMBAAECggEBAISDE1nk8F5J16SX68N4Tq/I5iPcegwajiKvP11PYynMtg+4QlhnUQjuaXp49dC1l7VBjqXAe8j8I+akocHRzN6VBEO12xNoL7bXYdTUEUaQiHFWrMbiZHcljxb7u0H1LVAjSDnaRLVZtp4Jpj/l4CsM4ZbFOr7bKVIWbgD0cUUF5ih7hM1ZvJanNXWMnIixOk3Ool1AzqpUUWRO7gU76mmHva9L48tX35/p0z2PrH407Cq64B1/5Nod5lyKo2+ajmuhFrQQ1UNxsNpvwlg/i7RcjVepY7EVIij97+rW7pVaYTZTXJgSpSLKY8hO0qf7fqFA33XHOJ7vaky/TPJzm4ECgYEA452F69GPgp0S6KYUNBnzjV3l0MvK2p7OQFnsfdknOARZ6922hYDAYGbWWU+PWR+9pV4llRLNCqWi2kgsNVzWack8bZthLJZyRSr6lf+yBESBUpsX9+Jwj/Pif0w/27+8IBQFEEBlUV9IF0eUlQq+kDC6xNQFZeiEUIAz4nercFcCgYEA0uxTvSClvesn9i57UJwjJpfhC7ztcZKw+QoTgnda7lNxEvfd1WikwOusZrCSx/9LpjyRkCUMa9j6hRaMr4wbvM12eaOsb2IdDeK8mbCSZsN0Lss38196sKCPlCfrqUPPUqB55tUU8Rglr/7Ur80jgGw9y+IPBxy3Z2d8a07/9NECgYALSZqEkUXeok93vhSuaMMNNaTZ7+FFai6sPkleDFDHlF+pNLuCb1oa7b6fezSOpOZQtxSCCgalCoXC8WVP2CTB5jra7KOrBGLyTylSGvYHBCatpAdSQaZ3XQ7UZUGdciqwJI4Duk3L9T+r997EbV355JC+hg0meptlKpZoGqsjzQKBgG0+qkCS4EV1vsTrXkNqG2qpz43c1L62U6MpbOFuIFLANRi1+NfH0014w1qMqfmcaPo49MvL5JsXTuoPu95Qmy023d4yv+UQ1CuU/Eo+AhXntg1mhBwxn8JL5xG9e3m3/XG2XG0KgEY/U4XMXyyO+4xQg9FNzKrKXNPZ7b9Gs5uBAoGBALyM491LPw+gwbVGrqZiJ2oKfKP08brqFLaZUog9Sh7I+brN8zuFiRbHi4EpXBEOKRsWqjrDNe3nBCcXcwZEhTtVKBPVXehPNKS31bjUF2Bxbpw49D6aUruBHW2YvgWwIMYnaxAHnLZVyLn2Rj+KSf804TDSZWoNk9wlMiI/tWxJ";
    private String ALI_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiTcFTcG86Bk/ZCA0TKqKyEYyKR6vVRhg/ivbbkfwVAIjibsTV/DiHGNN+tiGm6Uhmhu2hPUFYtsTqm668GvcR4nNPFMElbs4KtjUXgzb29q+8+DO9U/tsOkOQlE8P4N/Asurhj7HJbTXm+UIsU4mVCWuWGBb8pHdSgagdLQJGG5WYtTS03Lzh86lsw6oN0hvB/G2Iit248uPCM20Kbr/8Xx2FaXCZLUfpGXWgDSfB1wiFOwwi5tGaG7Zt+8N00/PQObu6mwfrz5dE+sEbU4UoiPj2v/+j3PdOLsUMdHerIoqUl0W2jFPigTzjufmPQgGnT8UW9+GJZABf/+wR14RKwIDAQAB";
    private String CHARSET = "UTF-8";
    private String SIGN_TYPE = AlipayConstants.SIGN_TYPE_RSA2;

    @Override
    public Boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception {
        return AlipaySignature.rsaCheckV1(reqData, ALI_PUBLIC_KEY, CHARSET, "RSA");
    }

    @Override
    public void refund(String outTradeNo, Integer refundMoney, Integer newPay) {
    	AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALI_PUBLIC_KEY, SIGN_TYPE);
		if (newPay != null && newPay == 1) {
			// 新支付
			alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", AliPayConfigNew.app_id,
					AliPayConfigNew.private_key, "json", CHARSET, AliPayConfigNew.ali_public_key,
					AliPayConfigNew.sign_type);

		}
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
        params.put("out_trade_no", outTradeNo);
        BigDecimal b1 = new BigDecimal(refundMoney);
	    BigDecimal divide = b1.divide(new BigDecimal(100));
        params.put("refund_amount", divide.doubleValue() + "");

        request.setBizContent(JSON.toJSONString(params));

        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            log.info("{} `s Refund response is {}", outTradeNo, response);

            Assert.isTrue(response.isSuccess(), "支付宝申请退款异常");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
