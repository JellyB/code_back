package com.huatu.ztk.pc.util;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.pc.common.ResponseMsg;
import com.huatu.ztk.pc.constants.ShareReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Created by huangqp on 2018\1\9 0009.
 */

public class RestTemplateUtil {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);
    private static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
    public static String URL_ROOT = "http://ns.huatu.com/";
    static {
        final String env = System.getProperty("disconf.env");
        if (env.equalsIgnoreCase("qa")) {//说明是测试环境,设置测试环境地址
            URL_ROOT = "http://192.168.100.22/";
        }
    }
    public static void main(String[] args){
        try {
//            getEssayReportByClient(2005356,"33a80ee7d16c4c2bac761d87288c4515","6.2","1");
            Object object = getTotalReportByClient(2005356,"33a80ee7d16c4c2bac761d87288c4515");
            logger.info("object={}",object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Map<String,Object> getEssayReportByClient(long paperId,String token,String cv,String terminal) throws Exception{
        ResponseEntity<ResponseMsg> resp = getEessayReporterRequest(paperId, token, cv, terminal);
        Map resultCard = (Map)resp.getBody().getData();
        Integer code = resp.getBody().getCode();
        Map result = (Map)resp.getBody().getData();
        if(code!=1000000){
            logger.info("code={},message={}",code,resp.getBody().getMsg());
            throw new BizException(ErrorResult.create(code,resp.getBody().getMsg()));
        }
        if(resultCard.get("expendTime")!=null){
            long expendTime = Long.parseLong(String.valueOf(resultCard.get("expendTime")));
            final Duration duration = Duration.ofSeconds(expendTime);
            long minutes = duration.toMinutes();
            final long seconds = duration.minusMinutes(minutes).getSeconds();
            result.put("minutes",minutes);
            result.put("seconds",seconds);
        }
        Map reportInfo = getReportWithStyle(resultCard, ShareReportType.ESSAYONLY);
        result.putAll(reportInfo);
        return result;
    }

    /**
     * 获取Essay模考统计数据的map
     * @param paperId
     * @param token
     * @param cv
     * @param terminal
     * @return
     */
    public static ResponseEntity<ResponseMsg> getEessayReporterRequest(long paperId, String token, String cv, String terminal) {
        logger.info("paperId={},token={},cv={},terminal={}",paperId,token,cv,terminal);
        String url = URL_ROOT + "e/api/v1/mock/report?paperId=" + paperId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE.toString());
        headers.add("token",token);
        headers.add("terminal",terminal);
        headers.add("cv",cv);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        return restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
    }

    /**
     * 获取所有模考统计的数据map
     * @param paperId
     * @param token
     * @return
     */
    public static ResponseEntity<ResponseMsg> getTotalReportRequest(long paperId, String token) {
        logger.info("paperId={},token={}",paperId,token);
        String url = URL_ROOT+ "p/v2/practices/total/" + paperId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("token",token);
        headers.add("terminal","1");
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        return restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
    }

    /**
     * 获取行测模考统计的数据map
     * @param paperId
     * @param token
     * @return
     */
    public static ResponseEntity<ResponseMsg> getMatchReportRequest(long paperId,String token,int version){
        logger.info("paperId={},token={}",paperId,token);
        String url = URL_ROOT+ "p/v"+version+"/practices/" + paperId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("token",token);
        headers.add("terminal","1");
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(headers);
        return restTemplate.exchange(url, HttpMethod.GET,requestEntity,ResponseMsg.class);
    }




    public static Map<String,Object> getTotalReportByClient(long paperId, String token) {
        ResponseEntity<ResponseMsg> resp = getTotalReportRequest(paperId, token);
        Map resultCard = (Map)resp.getBody().getData();
        Map reportInfo = getReportWithStyle(resultCard, ShareReportType.LINETESTWITHESSAY);
        return reportInfo;
    }


    private static Map getReportWithStyle(Map card, int type) {
        Map cardUserMeta = (Map)card.get("cardUserMeta");
        Map matchMeta = (Map)card.get("matchMeta");
        Map data = Maps.newHashMap();
        data.put("score", card.get("score"));
        data.put("totalRank", cardUserMeta.get("rank") + "/" + cardUserMeta.get("total"));
        data.put("positionRank", matchMeta.get("positionRank") + "/" + matchMeta.get("positionCount"));
        data.put("average", cardUserMeta.get("average"));
        data.put("matchMark", "mark");
        if ( type == ShareReportType.LINETESTWITHESSAY) {
            data.put("essayScore",card.get("essayScore"));
            data.put("lineTestScore",card.get("lineTestScore"));
        }
        return data;
    }
}
