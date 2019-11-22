package com.huatu.ztk.sms;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 漫道短信平台
 * @author hanchao
 * @date 2017/10/24 14:49
 */
public class MDSmsUtil {
    public static final String MD_API_URL = "http://sdk2.zucp.net/webservice.asmx/mt";
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000,TimeUnit.MILLISECONDS)
            .readTimeout(5000,TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100,5 * 60 * 1000, TimeUnit.MILLISECONDS))
            .followRedirects(true)//跟踪重定向
            .build();

    public static final String CAPTCHA_TEMPALTE = "【华图在线】验证码%s，您正在进行华图在线身份验证，打死不要告诉别人哦！";
    
    public static final String CAPTCHA_TEACHER_TEMPALTE = "【华图教师】验证码%s，您正在进行华图教师身份验证，打死不要告诉别人哦！";
    
    public static final int APP_TYPE_ON_LINE = 1;

    /**
     * 发送验证码
     * @param mobile
     * @param captcha
     * @param appType 1在线 2教师
     */
    public static void sendCaptcha(String mobile,String captcha,int appType){
        RequestBody requestBody = RequestBody.create(
                okhttp3.MediaType.parse("application/x-www-form-urlencoded"),
				getBody(Lists.newArrayList(mobile),
						String.format(appType == 1 ? CAPTCHA_TEMPALTE : CAPTCHA_TEACHER_TEMPALTE, captcha)));
        Request request = new Request.Builder().url(MD_API_URL).post(requestBody).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String data = response.body().string();
            //TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 在线发送验证码
     * @param mobile
     * @param captcha
     */
    public static void sendCaptcha(String mobile,String captcha){
    	sendCaptcha(mobile, captcha, APP_TYPE_ON_LINE);
    }

    /**
     * @param mobiles
     * @param content
     * @return
     */
    private static String getBody(List<String> mobiles, String content){
        Map<String,String> params = new HashMap();
        params.put("sn","SDK-BBX-010-22650");
        params.put("pwd","EFF7D598AD84203CB0E74B39562CED54");
        params.put("mobile", Joiner.on(",").join(mobiles));
        params.put("content",content);
        params.put("ext","");
        params.put("stime","");
        params.put("rrid","");
        params.put("msgfmt","");
        return Joiner.on("&").withKeyValueSeparator("=").join(params);
    }

    public static void main(String[] args){
        sendCaptcha("15652222294","1234");
    }
}
