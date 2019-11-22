//package com.huatu.tiku.position.biz;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.collect.Maps;
//import com.huatu.tiku.position.biz.domain.MsgUser;
//import com.huatu.tiku.position.biz.respository.MsgRepository;
//import com.huatu.tiku.position.biz.util.MapUtil;
//import lombok.extern.java.Log;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author wangjian
// **/
//@Component
//@Log
//public class task {
//
//    private MsgRepository msgRepository;
//
//    private RestTemplate restTemplate;
//
//    @Value("${msg.send}")
//    private String SEND_URL;
//
//    @Value("${msg.access}")
//    private String ACCESS_URL;
//
//    public task(MsgRepository msgRepository, RestTemplate restTemplate) {
//        this.msgRepository = msgRepository;
//        this.restTemplate = restTemplate;
//    }
//
//    private String getAccess_token(){
//        String access_url = ACCESS_URL;
//        ResponseEntity<String> forEntity = restTemplate.getForEntity(access_url, String.class);
//        String body = forEntity.getBody();
//        Map dto = JSON.parseObject(body, Map.class);
//        return  (String)dto.get("token");
//    }
//
//    @Scheduled(cron="*/30 * * * * ?")
//    private void process() {
//        log.info("开始执行");
//        List<MsgUser> users = msgRepository.getPositionMsgUser();//取出需要发送的用户集合
//        String access_token = getAccess_token();
//        String url = SEND_URL.replace("{access_token}", access_token);
//        for (MsgUser user : users) {
//            Map<String, Object> map = Maps.newHashMap();
//            map.put("touser", user.getOpenid());
//            map.put("template_id", "sLEqXwLwKZB1RWZDqA9JHTumJawA_l6uuQlbE100v_c");
//            Map data = MapUtil.of("first", MapUtil.of("value", user.getNickname() + "，您关注的2019国考报名入口即将开启", "color", "#173177"),
//                    "keyword1", MapUtil.of("value", "2019年国家公务员考试报名\n报名时间：9月13日 23:20-9月23日22:00", "color", "#173177"),
//                    "keyword2", MapUtil.of("value", "华图在线 [公务员职位库] 为您保留了意向职位，快去查看。", "color", "#173177"),
//                    "remark", "");
//            map.put("data", data);
//            String requestBody = JSONObject.toJSONString(map);
//            HttpHeaders headers = new HttpHeaders();
//            //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
//            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
//            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//            //  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
//            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
////            String body = stringResponseEntity.getBody();
////            body=new String(body.getBytes("ISO-8859-1"),"utf-8");
////            WechatSendMsgDto result= JSON.parseObject(body, WechatSendMsgDto.class);
////            if(null!=result&&"ok".equals(result.getErrmsg()) ){
////                System.out.println("success");
////            }
//        }
//    }
//}
