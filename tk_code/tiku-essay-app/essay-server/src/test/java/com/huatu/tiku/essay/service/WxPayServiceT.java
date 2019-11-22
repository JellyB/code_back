package com.huatu.tiku.essay.service;

import com.github.wxpay.sdk.WXPayUtil;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class WxPayServiceT extends TikuBaseTest {

    @Autowired
    private WxPayService wxPayService;

    @Test
    public void isPayResultNotifySignatureValid() throws Exception {
        String requestData = "<xml><appid><![CDATA[wx1a3fe9f7b7e6a86e]]></appid>\n" +
                "<bank_type><![CDATA[CFT]]></bank_type>\n" +
                "<cash_fee><![CDATA[1]]></cash_fee>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<is_subscribe><![CDATA[N]]></is_subscribe>\n" +
                "<mch_id><![CDATA[1509954481]]></mch_id>\n" +
                "<nonce_str><![CDATA[LR9XEHq6lwit0w3GLDPpzC7JNXkfN4i4]]></nonce_str>\n" +
                "<openid><![CDATA[o7p_N0SkeBTCAorBpxeMS_rfe4Vo]]></openid>\n" +
                "<out_trade_no><![CDATA[mk_0_t_131834_42]]></out_trade_no>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<sign><![CDATA[3DF76AA4B3C4D322732F8A81C0C688B78CD4BC296F4286476864301CBEBD74E8]]></sign>\n" +
                "<time_end><![CDATA[20190509131859]]></time_end>\n" +
                "<total_fee>1</total_fee>\n" +
                "<trade_type><![CDATA[APP]]></trade_type>\n" +
                "<transaction_id><![CDATA[4200000291201905093610438961]]></transaction_id>\n" +
                "</xml>";

        Map<String, String> requestDataMap = WXPayUtil.xmlToMap(requestData);

        boolean flag = wxPayService.isPayResultNotifySignatureValid(requestDataMap);

        log.info("Flag is {}", flag);
    }

    @Test
    public void refund() {
        wxPayService.refund("22441202480", "4200000342201907172285621772", 100, 100);
    }
}
