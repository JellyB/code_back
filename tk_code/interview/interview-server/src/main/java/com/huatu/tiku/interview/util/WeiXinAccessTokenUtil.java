package com.huatu.tiku.interview.util;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.entity.AccessToken;
import com.huatu.tiku.interview.entity.WeChatTemplate;
import com.huatu.tiku.interview.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static com.huatu.tiku.interview.util.HttpReqUtil.HttpDefaultExecute;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-03 下午6:20
 **/
@Slf4j
@Component
public class WeiXinAccessTokenUtil {
    @Autowired
    RestTemplate restTemplate;


    /**
     * 获取微信access_token
     * <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public AccessToken getWeiXinAccessToken() {
        AccessToken accessToken = restTemplate.getForObject(WeChatUrlConstant.TOKEN_URL, AccessToken.class);
        log.info("result:" + accessToken);
        return accessToken;
    }
    /**
     * 获取授权凭证token
     * @return json格式的字符串
     */
    public static String getAccessToken() {

        String json = HttpDefaultExecute(HttpReqUtil.GET_METHOD, WeChatUrlConstant.TOKEN_URL, null, "");
        log.info("获取accessToken，json ：{}",json);
        String result = null;
        AccessToken accessToken = JsonUtil.fromJson(json, AccessToken.class);
        if(accessToken != null){
            result = accessToken.getAccess_token();
            log.info("获取accessToken ,accessToken.getAccess_token() : {}",result);
        }
        return result;
    }
    /**
     * 发送模板消息
     */
    public void sendTemplateMessage(String accessToken, WeChatTemplate weChatTemplate) {
        String jsonString = new Gson().toJson(weChatTemplate).toString();
        String requestUrl = WeChatUrlConstant.TEMPLATE_NEWS_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, accessToken);


        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(jsonString, headers);

        String result =  restTemplate.postForObject(requestUrl,formEntity,String.class);
        log.info("result=" + result);
        JSONObject jsonObject =JSONObject.parseObject(result);
        log.info("jsonObject=" + jsonObject);
        if (null != jsonObject) {
            int errorCode = jsonObject.getIntValue("errcode");
            if (0 == errorCode) {
                log.info("模板消息发送成功");
            }else {
                String errorMsg = jsonObject.getString("errmsg");

            }
    }
}
}