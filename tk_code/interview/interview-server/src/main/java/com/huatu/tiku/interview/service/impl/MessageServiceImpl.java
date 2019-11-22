package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.config.WechatConfig;
import com.huatu.tiku.interview.service.MessageService;
import com.huatu.tiku.interview.util.HttpReqUtil;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/18 23:43
 * @Description
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public String sendTemplate(String accessToken, String data) {
        TreeMap<String,String> params = new TreeMap<String,String>();
        params.put("access_token", accessToken);
        String result = HttpReqUtil.HttpsDefaultExecute(HttpReqUtil.POST_METHOD, WechatConfig.SEND_MESSAGE, params, data);
        System.out.println(result);
        return result;
    }
}
