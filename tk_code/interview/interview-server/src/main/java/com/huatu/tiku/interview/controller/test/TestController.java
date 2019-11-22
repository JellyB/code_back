package com.huatu.tiku.interview.controller.test;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.entity.WeChatTemplate;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.template.TemplateData;
import com.huatu.tiku.interview.util.LogPrint;
import com.huatu.tiku.interview.util.WeiXinAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-03 上午11:11
 **/
@Slf4j
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TestController {

    @Autowired
    WeiXinAccessTokenUtil weiXinAccessTokenUtil;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("template")
    @LogPrint
    public void template(@RequestParam(required = false) String templateId) {
        templateId = templateId == null ? "NW9COwLjiGuv-xQpIoNpGtZ8M9zYLJxX6mo7Kt-6GbU" : templateId;
        WeChatTemplate wechatTemplate = new WeChatTemplate();
        Map<String, TemplateData> m = Maps.newHashMapWithExpectedSize(16);
        TemplateData first = new TemplateData();
        first.setColor("#FFC125");
        first.setValue("课程开课通知");
        m.put("first", first);

        TemplateData keyword1 = new TemplateData();
        keyword1.setColor("#EEE5DE");
        keyword1.setValue("高端面试课第一轮");
        m.put("keyword1", keyword1);

        TemplateData keyword2 = new TemplateData();
        keyword2.setColor("#000000");
        keyword2.setValue("2018年01月09日 15:00");
        m.put("keyword2", keyword2);

        TemplateData keyword3 = new TemplateData();
        keyword3.setColor("#000000");
        keyword3.setValue("北京市朝阳区蓝色港湾儿童城");
        m.put("keyword3", keyword3);
        TemplateData keyword4 = new TemplateData();
        keyword4.setColor("#000000");
        keyword4.setValue("18601270809");
        m.put("keyword4", keyword4);


        TemplateData remark = new TemplateData();
        remark.setColor("#EE4000");
        remark.setValue("高端面试课报道通知，如有疑问，请及时与我们取得联系~~~");
        m.put("remark", remark);

        wechatTemplate.setData(m);
        wechatTemplate.setTemplate_id(templateId);
        wechatTemplate.setTouser("okdNOuJ9Gzr-VRm4Om38WS5bVhho");
        wechatTemplate.setUrl("http://v.huatu.com");
        weiXinAccessTokenUtil.sendTemplateMessage(redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN), wechatTemplate);
        wechatTemplate.setTouser("okdNOuFFsk83SU9bvABn4rJinTvs");
        weiXinAccessTokenUtil.sendTemplateMessage(redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN), wechatTemplate);
    }

    @GetMapping("custom")
    @LogPrint
    public Object sendCustomNews() {
        log.info("sendCustomNews()");
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);

           String s = "{   \"touser\":\"okdNOuJ9Gzr-VRm4Om38WS5bVhho\",\"msgtype\":\"text\",\"text\":{\"content\":\"Hello World\"}}";
        String jsonString = new Gson().toJson(s).toString();
        String requestUrl = WeChatUrlConstant.CUSTOM_TEXT_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, at);


        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(jsonString, headers);

        String result =  restTemplate.postForObject(requestUrl,formEntity,String.class);
        log.info("result:"+result);
        return result;
    }
    @GetMapping("test1")
    @LogPrint
    public String test1(){
//        JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory)redisTemplate.getConnectionFactory();
//        jedisConnectionFactory.setDatabase(1);
//        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        String accessToken = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        System.out.println(accessToken);
        return accessToken;
    }

    @GetMapping("test2")
    @LogPrint
    public Result test2(HttpSession session){
        System.out.println(session.getAttribute("test1996"));
        return Result.ok("xx");
    }
}
