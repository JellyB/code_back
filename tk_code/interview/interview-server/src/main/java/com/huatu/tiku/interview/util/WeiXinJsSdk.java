package com.huatu.tiku.interview.util;

import com.google.common.collect.Maps;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.entity.AccessToken;
import com.huatu.tiku.interview.util.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Random;

import static com.huatu.common.utils.encrypt.EncryptUtil.sha1;
import static com.huatu.tiku.interview.constant.WeChatUrlConstant.TICKET_URL;
import static com.huatu.tiku.interview.util.HttpReqUtil.HttpDefaultExecute;

/**
 * Created by huangqp on 2018\5\24 0024.
 */
@Slf4j
@Data
@AllArgsConstructor
public class WeiXinJsSdk {
    private String appId;
    private String appSecret;
    private String url;

    public Map<String,String> getSignPackage(){
        String jsapiTicket = getJsApiTicket();
//        String protoclol = "http://";
        String url = this.url;
        Long timestamp = System.currentTimeMillis();
        String nonceStr = createNonceStr();
        String string = "jsapi_ticket="+jsapiTicket+"&noncestr="+nonceStr+"&timestamp="+timestamp+"&url="+url;
        String signature = sha1(string);
        Map<String,String> signPackage = Maps.newHashMap();
        signPackage.put("appId",this.appId);
        signPackage.put("nonceStr",nonceStr);
        signPackage.put("timestamp",timestamp+"");
        signPackage.put("url",url);
        signPackage.put("signature",signature);
        signPackage.put("rawString",string);
        return signPackage;
    }

    private String createNonceStr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String str = "";
        for (int i  = 0; i < 16; i++) {
            str += chars.charAt(new Random().nextInt(chars.length()-1));
        }
        return str;
    }

    public String getJsApiTicket() {
        String accessToken = WeiXinAccessTokenUtil.getAccessToken();
        String url = TICKET_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, accessToken);;
        String json = HttpDefaultExecute(HttpReqUtil.GET_METHOD, url, null, "");
        log.info("获取accessToken，json ：{}",json);
        Map map = JsonUtil.fromJson(json, Map.class);
        return map.getOrDefault("ticket","").toString();
    }
}

