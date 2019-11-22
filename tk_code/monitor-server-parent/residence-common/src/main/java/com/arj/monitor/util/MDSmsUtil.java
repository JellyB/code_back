package com.arj.monitor.util;


import com.arj.monitor.common.CommonResult;
import com.arj.monitor.exception.BizException;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author zhangchong
 *
 */
public class MDSmsUtil {

    static Logger LOG = LoggerFactory.getLogger(MDSmsUtil.class);

    public static final String MD_API_URL = "http://sdk.entinfo.cn:8061/webservice.asmx/mdgxsend";
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000,TimeUnit.MILLISECONDS)
            .readTimeout(5000,TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100,5 * 60 * 1000, TimeUnit.MILLISECONDS))
            .followRedirects(true)//跟踪重定向
            .build();


    public static  String sendMessage(String phoneNum, String content){
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                getBody(Lists.newArrayList(phoneNum),content));
        Request request = new Request.Builder().url(MD_API_URL).post(requestBody).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
			LOG.info("短信返回内容:{}", new String(response.body().bytes()));
            LOG.info( "手机号【{}】内容【{}】", phoneNum,content);
            return content;
        } catch (IOException e) {
            LOG.error("手机号【{}】内容【{}】短信发送失败，短信类型", phoneNum, content);
            throw new BizException(CommonResult.MESSAGE_SEND_ERROR);
        }
    }


    /**
     * @param mobiles
     * @param content
     * @return
     */
    private static String getBody(List<String> mobiles, String content){

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

    public static String getRandNum(int charCount) {
        String charValue = "";
        for (int i = 0; i < charCount; i++) {
            char c = (char) (randomInt(0, 10) + '0');
            charValue += String.valueOf(c);
        }
        return charValue;
    }

    private static int randomInt(int from, int to) {
        Random r = new Random();
        return from + r.nextInt(to - from);
    }


    public static String byte2Hex(byte b) {
        String[] h={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
        int i=b;
        if (i < 0) {i += 256;}
        return h[i/16] + h[i%16];
    }



}