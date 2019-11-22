package com.huatu.ztk.pc.service;

import com.huatu.ztk.pc.BaseTest;
import com.huatu.ztk.pc.common.ResponseMsg;
import com.huatu.ztk.pc.util.RestTemplateUtil;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by huangqp on 2018\1\9 0009.
 */

public class RestTest{
    private static final Logger logger = LoggerFactory.getLogger(RestTest.class);
    static HttpHeaders headers = new HttpHeaders();
    static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
    @Test
    public void test() throws Exception{
        String url = "http://192.168.100.22/e/v1/mock/report?paperId=346";
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("token","bc6650aa9af645e9857e37cb6fdf87c5");
        headers.add("terminal","1");
        headers.add("cv","6.1");
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> resp = restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
        Map<String,Object> result = (Map)resp.getBody().getData();
        for(Map.Entry<String,Object> entry:result.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            logger.info("key1={}",key);
            logger.info("key2={}",new String(key.getBytes("iso-8859-1"),"utf-8"));
            logger.info("value1={}",value);
            logger.info("value2={}",new String(value.toString().getBytes("iso-8859-1"),"utf-8"));
        }
    }
    @Test
    public void test1() throws Exception {
        RestTemplateUtil.getTotalReportByClient(3526707,"51cd1be4576a40a2bf48b1bded0e294e");
        RestTemplateUtil.getEssayReportByClient(330L,"51cd1be4576a40a2bf48b1bded0e294e","6.0","2");
    }
}
