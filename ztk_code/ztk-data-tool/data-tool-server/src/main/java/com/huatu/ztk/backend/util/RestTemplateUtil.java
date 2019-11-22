package com.huatu.ztk.backend.util;

import com.huatu.ztk.backend.metas.bean.MatchUserBean;
import com.huatu.ztk.backend.util.etag.DataStorm;
<<<<<<< HEAD
=======
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
>>>>>>> master
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\1\9 0009.
 */

public class RestTemplateUtil {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);
//    private static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//    public static String URL_ROOT = "http://192.168.10.37:11190/";
//    public static String URL_ROOT = "http://39.106.104.22/galaxy-api/";
    public static String URL_ROOT = "http://galaxy.htexam.com/galaxy-index/";
    public static final String STORM_URL  = URL_ROOT + "user/ztk/storm";
//    static {
//        final String env = System.getProperty("disconf.env");
//        if (env.equalsIgnoreCase("online")) {//说明是测试环境,设置测试环境地址
//            URL_ROOT = "http://192.168.10.60:11188/";
//        }
//    }

    /**
     * 模板
     */
    public void test(){
        HttpHeaders headers = new HttpHeaders();
        //请求接受格式
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        //添加加密验证这个必须有
        headers.add("Authorization", "Basic amJ6bTp6aGVuZ3lpd29haW5p");
        //拼接
        DataStorm dataStorm = new DataStorm();
        HttpEntity<DataStorm> formEntity = new HttpEntity<>(dataStorm, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> responseEntity = restTemplate.exchange("url", HttpMethod.GET,formEntity,Object.class);
    }
    public static boolean postDuplicateClear(int newId,int oldId,String pandora){
        String url = "http://"+pandora+"/pand/question/v1/duplicate/clear?newId="+newId+ "&oldId="+oldId;
        System.out.println("newId = [" + newId + "], oldId = [" + oldId + "], pandora = [" + pandora + "]");
        System.out.println("url = " + url);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.POST,null,Object.class);
        return true;
    }
    public static Object getIndex(int type){
        String url = URL_ROOT + "index/findCursor?type="+type;
        logger.info("url={}",url);
        HttpHeaders headers = new HttpHeaders();
        //请求接受格式
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        //添加加密验证这个必须有
        headers.add("Authorization", "Basic amJ6bTp6aGVuZ3lpd29haW5p");
        //拼接
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.GET,requestEntity,Object.class);
        Object resp =  responseEntity.getBody();
        return resp;
    }
    public static boolean postLogs(List<Map<String,Object>> data,String url){
//        String url = URL_ROOT + "user/ztk/storm";
        HttpHeaders headers = new HttpHeaders();
        //请求接受格式
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        //添加加密验证这个必须有
        headers.add("Authorization", "Basic amJ6bTp6aGVuZ3lpd29haW5p");
        //拼接
        DataStorm dataStorm = DataStorm.builder().index("galaxy_match").type("user_match").data(data).offsetType(1).dataType("elasticsearch").build();
        HttpEntity<DataStorm> formEntity = new HttpEntity<>(dataStorm, headers);
        RestTemplate restTemplate = new RestTemplate();
        try{
            restTemplate.exchange(url, HttpMethod.POST,formEntity,Object.class);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
    public static boolean postLog(List<MatchUserBean> logs){
        String url = URL_ROOT + "user/ztk/storm";
        HttpHeaders headers = new HttpHeaders();
        //请求接受格式
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        //添加加密验证这个必须有
        headers.add("Authorization", "Basic amJ6bTp6aGVuZ3lpd29haW5p");
        //拼接
        DataStorm dataStorm = DataStorm.builder().index("1").type("1").data(null).build();
        HttpEntity<DataStorm> formEntity = new HttpEntity<>(dataStorm, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.POST,formEntity,Object.class);
//        ResponseMsg<Object> resp = restTemplate.postForObject(url,logs,ResponseMsg.class);
        if(responseEntity==null||responseEntity.getBody()==null||!responseEntity.getBody().toString().contains("1000000")){
            logger.info("post request is failed,message = {}",responseEntity.getBody());
            return  false;
        }
        return true;
    }

    public static void import2Mongo(List<Integer> paperIds) {
        for (Integer paperId : paperIds) {
//            String url = URL_ROOT+ "p/v2/practices/total/" + paperId;
            String url = "http://192.168.100.24:17451/p/import2mongo?paperId=" + paperId;
            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
//            headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
//            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
//            ResponseEntity<ResponseMsg> resp = restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
//            Map resultCard = (Map)resp.getBody().getData();
            Object resultCard = restTemplate.getForObject(url,Object.class);
            logger.info("resultCard={}",resultCard);
        }
    }
    public static void importTest2Mongo(List<Integer> paperIds) {
        for (Integer paperId : paperIds) {
//            String url = URL_ROOT+ "p/v2/practices/total/" + paperId;
            String url = "http://192.168.100.25:17451/p/importTestPaper2mongo?paperId=" + paperId;
            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
//            headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
//            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
//            ResponseEntity<ResponseMsg> resp = restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
//            Map resultCard = (Map)resp.getBody().getData();
           Object resultCard = restTemplate.getForObject(url,Object.class);
            logger.info("resultCard={}",resultCard);
        }
    }

    /**
     * {{pandora}}/pand/util/paper/detail?paperId=851(试卷查询详情)
     * @param paper
     */
    public static void sync2Mysql(Paper paper) {
        String url = "http://192.168.100.22:11145/pand/util/paper/detail?paperId=" + paper.getId();
        RestTemplate restTemplate = new RestTemplate();
        try{
            Object resultCard = restTemplate.getForObject(url,Object.class);
            String s = JsonUtil.toJson(resultCard);
            logger.info("resultCard={}", s.substring(1,Math.min(s.length(),100))+"……");
        }catch (Exception e){
            logger.error("sync2Mysql error,paperId = {}",paper.getId());
        }

    }
}
