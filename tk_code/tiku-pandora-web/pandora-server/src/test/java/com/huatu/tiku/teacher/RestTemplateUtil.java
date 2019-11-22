package com.huatu.tiku.teacher;

import com.huatu.tiku.util.http.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\1\9 0009.
 */
@Slf4j
public class RestTemplateUtil {

    private static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//    public static String URL_ROOT = "http://ns.huatu.com/";
    public static String URL_ROOT = "http://192.168.100.22:17458/";
//    static {
//        final String env = System.getProperty("disconf.env");
//        if (env.equalsIgnoreCase("qa")) {//说明是测试环境,设置测试环境地址
//            URL_ROOT = "http://192.168.100.22/";
//        }
//    }
    public static void main(String[] args){
        try {
            findCateGories();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Map> findKnowledge(int subject) {
        String url = URL_ROOT + "question/point/all";
        if(subject!=-1){
            url = url + "?subject="+subject;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
        List<Map> knowledgeList = (List<Map>)exchange.getBody().getData();
        for (Map knowledge : knowledgeList) {
            System.out.println(knowledge.get("id")+"\t"+knowledge.get("name")+"\t"+knowledge.get("subject")+"\t"+knowledge.get("level")+"\t"+knowledge.get("parent"));
        }
        return knowledgeList;
    }

    /**
     * 获取Essay模考统计数据的map
     * @return
     */
    public static List<Map> findSubjects() {
        String url = URL_ROOT + "subject/all";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
        List<Map> subjects = (List<Map>)exchange.getBody().getData();
        for (Map subject : subjects) {
            System.out.println(subject.get("id")+"\t"+subject.get("name")+"\t"+subject.get("catgory")+"\t"+subject.get("status"));
        }
        return subjects;
    }

    /**
     * 获取Essay模考统计数据的map
     * @return
     */
    public static List<Map> findCateGories() {
        String url = URL_ROOT + "catgory/";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<ResponseMsg> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ResponseMsg.class);
        List<Map> subjects = (List<Map>)exchange.getBody().getData();
        for (Map subject : subjects) {
            System.out.println(subject.get("id")+"\t"+subject.get("name")+"\t"+subject.get("catgory")+"\t"+subject.get("status"));
        }
        return subjects;
    }


}
