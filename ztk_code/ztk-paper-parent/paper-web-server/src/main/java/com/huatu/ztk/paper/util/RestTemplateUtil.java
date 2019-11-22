package com.huatu.ztk.paper.util;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.common.ResponseMsg;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 调用新模考大赛的入口
 */
public class RestTemplateUtil {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);
    private static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
    public static String URL_ROOT = "http://ns.huatu.com/";
    static {
        final String env = System.getProperty("disconf.env");
        if (StringUtil.isNullOrEmpty(env) || env.equalsIgnoreCase("qa")) {//说明是测试环境,设置测试环境地址
            URL_ROOT = "http://192.168.100.22/";
        }
    }
    public static void main(String[] args){
        try {
            ResponseEntity<ResponseMsg> responseEntity = matches("483612d254354dcd9e832fcdbe5aafed32fcdbe5aafed", 1);
//            ResponseEntity<ResponseMsg> responseEntity = createAnswerCard("deb83871a252469da03962d4eca2eb5f", 3528409, 1);
//            ResponseEntity<ResponseMsg> responseEntity = getReport("deb83871a252469da03962d4eca2eb5f", 3528400);
            ResponseMsg body = responseEntity.getBody();
            System.out.println(body.getData());
//            Object data = MatchResponseUtil.getReport("deb83871a252469da03962d4eca2eb5f", 3528409);
            Object data = MatchResponseUtil.createAnswerCard("483612d254354dcd9e832fcdbe5aafed", null, 1);
            logger.info("object=" + data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询模考大赛首页接口
     * @param token
     * @param subject
     * @return
     * @throws BizException
     */
    public static ResponseEntity<ResponseMsg> matches(String token,int subject) throws BizException {
        String url = URL_ROOT + "match/v1/search";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        headers.add("token",token);
        headers.add("subject",String.valueOf(subject));
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
        return exchange;
    }

    /**
     * 创建答题卡外部接口调用
     * @param token
     * @param paperId
     * @param terminal
     * @return
     */
    public static ResponseEntity<ResponseMsg> createAnswerCard(String token, int paperId, int terminal) {
        String url = URL_ROOT + "match/v1/answerCard/" + paperId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        headers.add("token",token);
        headers.add("terminal",String.valueOf(terminal));
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ResponseMsg.class);
        return exchange;

    }

    public static ResponseEntity<ResponseMsg> getReport(String token, int paperId) {
        String url = URL_ROOT + "match/v1/practices/" + paperId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        headers.add("token",token);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
        return exchange;
    }
}
