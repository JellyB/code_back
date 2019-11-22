package com.huatu.tiku.interview.controller.api.v1;

import com.google.gson.Gson;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.util.LogPrint;
import com.huatu.tiku.interview.util.WeiXinAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-05 下午5:36
 **/
@Slf4j
@RestController
@RequestMapping(value = "/api/messages",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MsgController {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    WeiXinAccessTokenUtil weiXinAccessTokenUtil;


    @LogPrint
    @PostMapping("mass")
    public void sendMassMessages(){
    String msg = "{\"touser\":[\"o4gqK1rXJU9B5t8qHc59S0SAuuA8\",\"o4gqK1rOtTxZ-cGlmHF5oyx6he58\"], \"msgtype\": \"text\",\"text\": { \"content\": \"谁让你这么早下班的？？？."+ UUID.randomUUID()+"\"} }";
        String jsonString = new Gson().toJson(msg).toString();
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        String requestUrl = WeChatUrlConstant.MESSAGE_MANY_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, at);


        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(msg, headers);

        String result =  restTemplate.postForObject(requestUrl,formEntity,String.class);
        log.info("result:"+result);
    }

    @PostMapping("custom")
    @LogPrint
    public void sendCustomMessages(){
    String msg = "{\"touser\":\"o4gqK1rOtTxZ-cGlmHF5oyx6he58\", \"msgtype\":\"text\",\"text\":{ \"content\": \"谁让你这么早下班的？？？."+ UUID.randomUUID()+"\"} }";
        String jsonString = new Gson().toJson(msg).toString();
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        String requestUrl = WeChatUrlConstant.MESSAGE_MANY_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, at);



        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(msg, headers);

        String result =  restTemplate.postForObject(requestUrl,formEntity,String.class);
        log.info("result:"+result);
    }



}
