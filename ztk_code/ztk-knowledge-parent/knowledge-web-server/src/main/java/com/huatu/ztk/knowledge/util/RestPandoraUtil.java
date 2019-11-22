package com.huatu.ztk.knowledge.util;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.common.ResponseMsg;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class RestPandoraUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestPandoraUtil.class);
    public static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
    public static String URL_ROOT = "http://ns.huatu.com/";

    static {
        final String env = System.getProperty("disconf.env");
        if (StringUtil.isNullOrEmpty(env) || !env.equalsIgnoreCase("online")) {//说明是测试环境,设置测试环境地址
            URL_ROOT = "http://192.168.100.22/";
        }
    }

    /**
     * 知识点下试题数量查询接口
     */
    public static final String POINT_QUESTION_COUNT = "pand/sy/point/count";
    /**
     * 知识点下试题ID查询接口
     */
    public static final String POINT_QUESTION_LIST = "pand/sy/point/list";

    public static ResponseEntity<ResponseMsg> getSysPointQuestion(String path){
        try{
            String url = URL_ROOT + path;
            logger.info("getSysPointQuestion.url={}", url);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
            ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
            return exchange;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Function<ResponseEntity<ResponseMsg>, LinkedHashMap> getData = (result -> {
        if (null == result || null == result.getBody()) {
            return null;
        }
        ResponseMsg body = result.getBody();
        if (body.getCode() != 1000000) {
            logger.info("body.code={}", body.getCode());
            return null;
        }
        Object data = body.getData();
        if (data instanceof LinkedHashMap) {
            return (LinkedHashMap) data;
        }
        logger.info("data's class = {}", data.getClass());
        return null;

    });

    public static void main(String[] args) throws BizException {
//        ResponseEntity<ResponseMsg> sysPointQuestion = getSysPointQuestion("/pand/sy/point/list");
        ResponseEntity<ResponseMsg> sysPointQuestion = getSysPointQuestion("/pand/sy/point/count");
        LinkedHashMap apply = getData.apply(sysPointQuestion);
        System.out.println(JsonUtil.toJson(apply));
    }
}

