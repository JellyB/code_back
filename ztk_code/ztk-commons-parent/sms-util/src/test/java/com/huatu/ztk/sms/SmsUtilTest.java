package com.huatu.ztk.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-03 15:58
 */
public class SmsUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(SmsUtilTest.class);

    public static void main(String[] args) {
        String content = "注册码：" + 245869 + "，请尽快激活！如有疑问，请拨打400-678-1009，华图网校感谢您的支持。";
//        SmsUtil.sendCaptcha("13717670214", 245869+"");
        List<String> mobiles = new ArrayList();
        mobiles.add("13717670214");
        mobiles.add("13717670214");
        SmsUtil.sendWarn(mobiles,"web-server",100);
    }
}
