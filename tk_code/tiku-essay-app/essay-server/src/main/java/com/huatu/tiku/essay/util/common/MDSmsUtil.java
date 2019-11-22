package com.huatu.tiku.essay.util.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MDSmsUtil {
    public static final String MD_API_URL = "http://sdk2.zucp.net/webservice.asmx/mt";
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100, 5 * 60 * 1000, TimeUnit.MILLISECONDS)).followRedirects(true)// 跟踪重定向
            .build();

    private static String sendSMSOrderTemplate;

    private static String phoneStr;


    @Value("${send_sms_order_admin_phone}")
    public void setPhoneStr(String phone) {
        this.phoneStr = phone;
    }

    public static final String ORDER_RETURN_TEMPALTE = "【华图在线】您批改的订单被管理员撤回了，详情请申论登录后台查看。";

    public static final String ADMIN_ORDER_TEMPLATE = "【华图在线】任务管理员，您有新的申论批改订单待分配，任务类型【%s】，题目【%s】，请尽快分配批改老师~目前待分配订单%s个~";

    public static final String DISPATCH_ORDER_TEMPLATE = "【华图在线】您有新的申论批改订单，任务类型【%s】，题目【%s】，请尽快接单批改~";


    /**
     * 读取配置
     *
     * @param template
     */
    @Value("${send_sms_order_template}")
    public void setSendSMSOrderTemplate(String template) {
        this.sendSMSOrderTemplate = template;
    }


    public static void sendAdminOrderMsg(String typeName, String stem, Integer count) {
        String format = String.format(ADMIN_ORDER_TEMPLATE, typeName, stem, count.toString());
        log.info("手机号:{}短信尝试发送:{}", phoneStr, format);
        Optional.ofNullable(phoneStr)
                .map(i -> Arrays.stream(i.split(",")).filter(NumberUtils::isDigits).collect(Collectors.toList()))
                .filter(i -> i.size() > 0)
                .ifPresent(i -> {
                    i.forEach(s -> sendMsg(s, format));
                });
    }


    /**
     * 通用发送消息模版
     *
     * @param mobile
     * @param template
     */
    public static void sendMsg(String mobile, String template) {

        RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("application/x-www-form-urlencoded"),
                getBody(Lists.newArrayList(mobile), template));
        Request request = new Request.Builder().url(MD_API_URL).post(requestBody).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String data = response.body().string();
            log.info("手机号:{}短信发送结果:{}", mobile, data);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("短信发送失败手机号:{}", mobile);
        }

    }

    /**
     * 发送接单消息
     *
     * @param mobile
     */
    public static void sendCorrectOrderMsg(String mobile) {
        if (StringUtils.isNotBlank(mobile)) {
            sendMsg(mobile, sendSMSOrderTemplate);
        }
    }

    /**
     * 发送管理员撤回工单消息
     *
     * @param mobile
     */
    public static void sendReturnMsg(String mobile) {
        if (StringUtils.isNotBlank(mobile)) {
            sendMsg(mobile, ORDER_RETURN_TEMPALTE);
        }
    }

    /**
     * @param mobiles
     * @param content
     * @return
     */
    private static String getBody(List<String> mobiles, String content) {
        Map<String, String> params = new HashMap();
        params.put("sn", "SDK-BBX-010-22650");
        params.put("pwd", "EFF7D598AD84203CB0E74B39562CED54");
        params.put("mobile", Joiner.on(",").join(mobiles));
        params.put("content", content);
        params.put("ext", "");
        params.put("stime", "");
        params.put("rrid", "");
        params.put("msgfmt", "");
        return Joiner.on("&").withKeyValueSeparator("=").join(params);
    }
}
