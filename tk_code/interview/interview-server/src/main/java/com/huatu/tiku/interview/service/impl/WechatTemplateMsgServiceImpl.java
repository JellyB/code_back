package com.huatu.tiku.interview.service.impl;


import com.huatu.tiku.interview.config.WechatConfig;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.entity.template.TemplateMsgResult;
import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.util.HttpReqUtil;
import com.huatu.tiku.interview.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:30
 * @Description
 */
@Service
@Slf4j
public class WechatTemplateMsgServiceImpl implements WechatTemplateMsgService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    /**
     * 发送模板消息
     * @param data
     * @return 状态
     */
    @Override
    public TemplateMsgResult sendTemplate( String data) {
        TemplateMsgResult templateMsgResult = null;
        TreeMap<String,String> params = new TreeMap<String,String>();
        //token推送的时候，实时获取
       String accessToken = stringRedisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        params.put("access_token", accessToken);
        String result = HttpReqUtil.HttpsDefaultExecute(HttpReqUtil.POST_METHOD, WechatConfig.SEND_TEMPLATE_MESSAGE, params, data);
        templateMsgResult = JsonUtil.fromJson(result, TemplateMsgResult.class);
        log.info("推送消息返回信息:{}",result);
        return templateMsgResult;
    }
}