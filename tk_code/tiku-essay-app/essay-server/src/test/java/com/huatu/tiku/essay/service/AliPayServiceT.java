package com.huatu.tiku.essay.service;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.essay.service.impl.AliPayServiceImpl;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class AliPayServiceT extends TikuBaseTest {

    @Autowired
    private AliPayService aliPayService = new AliPayServiceImpl();

    @Test
    public void isPayResultNotifySignatureValid() throws Exception {
        String content = "{\"gmt_create\":\"2019-05-09 08:41:52\",\"charset\":\"UTF-8\",\"seller_email\":\"htwx@huatu.com\",\"subject\":\"测试商品-1\",\"sign\":\"JUxoCBVmGcrPwJRh68MjtjXXT2g5SnYjRJnSTjs41YI90R4eJg2dauNdqgHl2k4rXYuwddGFYPhsjbg3xQdxI6oVpom7uIeOamGJX/+PYCaEN5n9eU+z28XpHrAFn5QV0iXCOIcuFWM/yFpkE0EC6akK5tr9HBi1dFazzG7Pg/RY5AnfdHugUHf+ubYXxYUjVFkzIX6hjh4ZYVyo//fNRn7QaIvSjd+sPQCFIveCfX+XLXa5QXG1tMCuBzXblQVSg7ncqx6wro/UW09cVxiHfJ1CBcUtVZJMPLjDPdgbXOwhirD8zhkNMKRutm9HQBxvZa2HWUXfFD91bY4TQHIg6A==\",\"body\":\"测试商品-1\",\"buyer_id\":\"2088502940843456\",\"invoice_amount\":\"0.01\",\"notify_id\":\"2019050900222084152043451021812894\",\"fund_bill_list\":\"[{\\\"amount\\\":\\\"0.01\\\",\\\"fundChannel\\\":\\\"PCREDIT\\\"}]\",\"notify_type\":\"trade_status_sync\",\"trade_status\":\"TRADE_SUCCESS\",\"receipt_amount\":\"0.01\",\"app_id\":\"2019050664384058\",\"buyer_pay_amount\":\"0.01\",\"sign_type\":\"RSA2\",\"seller_id\":\"2088411127750624\",\"gmt_payment\":\"2019-05-09 08:41:52\",\"notify_time\":\"2019-05-09 09:06:06\",\"version\":\"1.0\",\"out_trade_no\":\"mk_0_t_08414331\",\"total_amount\":\"0.01\",\"trade_no\":\"2019050922001443451041666292\",\"auth_app_id\":\"2019050664384058\",\"buyer_logon_id\":\"131****4400\",\"point_amount\":\"0.00\"}";

        Map<String, String> reqData = JSONObject.parseObject(content, Map.class);

        boolean flag = aliPayService.isPayResultNotifySignatureValid(reqData);

        log.info("Flag is {}", flag);
    }

    @Test
    public void refund() {
        aliPayService.refund("461", 1, 1);
    }
}
