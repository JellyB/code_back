package com.huatu.ztk.sms;

import com.taobao.api.ApiException;
import com.taobao.api.AutoRetryClusterTaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 发送短信的工具包
 * Created by shaojieyue
 * Created time 2016-06-03 15:42
 */
public class SmsUtil {
    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);
    private static final String TAOBAO_GW = "http://gw.api.taobao.com/router/rest";
    public static final String appkey = "23438825";
    public static final String secret = "1491f7a7340a53c18ea8ea9709ac8ad4";
    public static final String MOBILE_KEY = "mobile";
    public static final BlockingQueue<AlibabaAliqinFcSmsNumSendRequest> queue = new LinkedBlockingQueue<AlibabaAliqinFcSmsNumSendRequest>(200);
    public static volatile boolean running = true;

    private static AutoRetryClusterTaobaoClient taobaoClient;

    static {
        try {
            taobaoClient = new AutoRetryClusterTaobaoClient(TAOBAO_GW, appkey, secret,"json",1000,2000);
            taobaoClient.setMaxRetryCount(2);//重试2次
            taobaoClient.setRetryWaitTime(100);//等待100ms
        } catch (ApiException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                running = false;
            }
        }));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (;running || !queue.isEmpty();){
                    try {
                        AlibabaAliqinFcSmsNumSendRequest request = null;
                        request = queue.take();
                        if (request == null) {
                            continue;
                        }
                        long start = System.currentTimeMillis();
                        final AlibabaAliqinFcSmsNumSendResponse response = taobaoClient.execute(request);
                        logger.info("send complete sms to mobile={},data={}",request.getRecNum(),request.getSmsParam());
                        long time = System.currentTimeMillis()-start;
                        if (time > 1000) {
                            logger.warn("send sms cost time={}",time);
                        }
                    }catch (Exception e){
                        logger.error("ex",e);
                    }

                }
            }
        }).start();
    }

    /**
     * 验证码接口
     * @param mobile   电话号码
     * @param captcha 验证码
     * @return
     */
    public static void sendCaptcha(String mobile, String captcha) {
        Map params = new HashMap<>();
        params.put("code", captcha);
        params.put("product", "砖题库");

        sendSms(Arrays.asList(mobile), params, "SMS_13246682");
    }

    /**
     * 发送短信服务报警
     * @param mobiles 手机号列表
     * @param serverName
     * @param count
     */
    public static final void sendWarn(List<String> mobiles, String serverName, int count){

        //报警时间
        final String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm:ss"));

        Map params = new HashMap<>();
        params.put("time", time);
        params.put("serverName", serverName);
        params.put("count", count);
        sendSms(mobiles,params,"SMS_40170004");
    }

    /**
     * 发送短信的接口
     * @param params 参数列表,每条短信一个map,里面必须包含一个mobile key
     * @param templateCode 模板代码
     */
    public static final void sendSms(List<Map<String,Object>> params,String templateCode){
        for (Map<String, Object> param : params) {
            final Object mobile = param.remove(MOBILE_KEY);
            if (mobile == null || !(mobile instanceof String)) {
                throw new RuntimeException("手机号非法");
            }
            AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
            req.setExtend("");
            req.setSmsType("normal");
            req.setSmsFreeSignName("砖题库");
            req.setSmsParamString(getSmsParamString(param));
            req.setRecNum((String)mobile);
            req.setSmsTemplateCode(templateCode);
            queue.add(req);
        }
    }


    private static final void sendSms(List<String> mobiles, Map<String,Object> params,String templateCode) {
        for (String mobile : mobiles) {
            AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
            req.setExtend("");
            req.setSmsType("normal");
            req.setSmsFreeSignName("砖题库");
            req.setSmsParamString(getSmsParamString(params));
            req.setRecNum(mobile);
            req.setSmsTemplateCode(templateCode);
            queue.add(req);
        }
    }

    private static String getSmsParamString(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder();
        params.keySet().stream()
                .filter(key -> key != null)
                .forEach(key -> builder.append(key + ":'" + params.get(key) + "',"));
        return "{" + builder.substring(0, builder.length() - 1) + "}";
    }
}
