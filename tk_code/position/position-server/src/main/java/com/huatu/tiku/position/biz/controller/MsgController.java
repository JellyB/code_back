package com.huatu.tiku.position.biz.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.position.biz.domain.MsgUser;
import com.huatu.tiku.position.biz.dto.*;
import com.huatu.tiku.position.biz.respository.MsgRepository;
import com.huatu.tiku.position.biz.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author wangjian
 **/
@RestController
@RequestMapping("msg")
@Slf4j
public class MsgController {

    @Value("${msg.access}")
    private String ACCESS_URL;

    @Value("${msg.url}")
    private String OPEN_URL;

    @Value("${msg.posturl}")
    private String POST_URL;

    @Value("${msg.send}")
    private String SEND_URL;

    private RestTemplate restTemplate;

    private MsgRepository msgRepository;

    @Autowired
    public MsgController(RestTemplate restTemplate , MsgRepository msgRepository) {
        this.restTemplate = restTemplate;
        this.msgRepository = msgRepository;
    }

    private String getAccess_token(){
        String access_url = ACCESS_URL;
        ResponseEntity<String> forEntity = restTemplate.getForEntity(access_url, String.class);
        String body = forEntity.getBody();
        Map dto = JSON.parseObject(body, Map.class);
       return  (String)dto.get("token");
    }

    private String getAccess_token(Boolean flag){
        String access_url = ACCESS_URL;
        if(flag){
            access_url=access_url+"?reset=1";
        }
        ResponseEntity<String> forEntity = restTemplate.getForEntity(access_url, String.class);
        String body = forEntity.getBody();
        Map dto = JSON.parseObject(body, Map.class);
       return  (String)dto.get("token");
    }

    /**
     * 获取用户列表 (关注用户openId)根据最后一个openId拉取
     */
    @GetMapping("getUserList")
    public Object getUserList(String next_openid){
        String access_token=getAccess_token();

//        MsgUser msgUser = msgRepository.findlastOpenId();//数据库中取出的最后一个openId
//        String next_openid=null;
//        if(null!=msgUser){
//            next_openid=msgUser.getOpenid();
//        }

        String url = OPEN_URL.replace("{access_token}", access_token);
        if(StringUtils.isNotBlank(next_openid)){
            url = url.replace("{next_openid}", next_openid);
        }else{
            url = url.replace("{next_openid}", "");
        }
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        MsgOpenIdData msgOpenIdData = JSON.parseObject(body, MsgOpenIdData.class);
        if(!StringUtils.isNotBlank(msgOpenIdData.getErrcode())){
            MsgOpenIdData.Data data = msgOpenIdData.getData();
            List<String> openIdList = data.getOpenid();
            List<MsgUser> users= Lists.newArrayList();
            for (String openId : openIdList) {
                log.info(openId);
                MsgUser user=new MsgUser();
                user.setOpenid(openId);
//                users.add(user);
                try {
                    msgRepository.save(user);
                } catch (Exception e) {

                }
            }
        }
        return true;
    }

    /**
     * 根据关注列表拉取用户
     */
    @GetMapping("getListUserInfo")
    public Object getListUserInfo() throws UnsupportedEncodingException {
        String access_token = getAccess_token();
        int i=0;
        while(getUserInfo(i,access_token)){
            System.out.println("第"+i+"页");
            i++;
        }
        return true;
    }

    private Boolean getUserInfo(Integer page,String access_token) throws UnsupportedEncodingException {
        Pageable pageable = new PageRequest(page, 100);
//        Page<MsgUser> all = msgRepository.findAll(pageable);
        Page<MsgUser> all = msgRepository.findBySubscribe(null,pageable);
        List<MsgUser> content = all.getContent();
        Map<String,List> map= Maps.newHashMap();
        List<Map> list= Lists.newArrayList();
        for (MsgUser msgUser : content) {
            Map bean=Maps.newHashMap();
            bean.put("lang","zh_CN");
            bean.put("openid",msgUser.getOpenid());
            list.add(bean);
        }
        map.put("user_list",list);
        String requestBody = JSONObject.toJSONString(map);
        HttpHeaders headers = new HttpHeaders();
//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
//        String access_token=getAccess_token();
        String url = POST_URL.replace("{access_token}", access_token);
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String body = stringResponseEntity.getBody();
        body=new String(body.getBytes("ISO-8859-1"),"utf-8");
        MsgListUserInfoDto result= JSON.parseObject(body, MsgListUserInfoDto.class);
        if(!StringUtils.isNotBlank(result.getErrcode())) {
            List<MsgUser> msgUserLists=Lists.newArrayList();
            List<MsgUserDto> user_info_list = result.getUser_info_list();
            for (MsgUserDto msgUserDto : user_info_list) {
                if("1".equals(msgUserDto.getSubscribe())){  //关注状态才有信息
                    Optional<MsgUser> MsgUser = content.stream().filter(bean->bean.getOpenid().equals(msgUserDto.getOpenid())).findFirst();
                    MsgUser msgUser = MsgUser.get();
                    BeanUtils.copyProperties(msgUserDto, msgUser);
                    msgUser.setNickname(msgUser.getNickname().replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", ""));
                    msgUserLists.add(msgUser);
                }else{
                    Optional<MsgUser> MsgUser = content.stream().filter(bean->bean.getOpenid().equals(msgUserDto.getOpenid())).findFirst();
                    MsgUser msgUser = MsgUser.get();
                    BeanUtils.copyProperties(msgUserDto, msgUser);
                    msgUserLists.add(msgUser);
                }
            }
            msgRepository.save(msgUserLists);
        }
        return !all.isLast();
    }

    /**
     * 定时给指定用户发送模板消息
     */
    @GetMapping("sendMsg")
    public void sendMsg() throws UnsupportedEncodingException {
        List<MsgUser> users=msgRepository.getPositionMsgUser();//取出需要发送的用户集合
        String access_token = getAccess_token();
        String url = SEND_URL.replace("{access_token}", access_token);
        List<MsgUser> failUsers=Lists.newArrayList();
        for (MsgUser user : users) {
            if(null==user||null==user.getOpenid()){
                continue;
            }
            Map<String,Object> map= Maps.newHashMap();
            map.put("touser",user.getOpenid());
            map.put("template_id","sLEqXwLwKZB1RWZDqA9JHTumJawA_l6uuQlbE100v_c");
            map.put("miniprogram",MapUtil.of("appid","wxf944787a26c46ba5","pagepath","pages/userInfo/index?notice=true"));
            Map data= MapUtil.of("first",MapUtil.of("value",user.getNickname()+"，您关注的2019国考报名入口即将关闭","color","#173177"),
                                "keyword1",MapUtil.of("value","2019年国家公务员考试报名\n报名时间：10月22日 8:00-10月31日18:00","color","#173177"),
                                "keyword2","",
                                "remark",MapUtil.of("value","华图在线 [公务员职位库] 为您保留了意向职位，快去查看。","color","#173177"));
            map.put("data",data);
            String requestBody = JSONObject.toJSONString(map);
            HttpHeaders headers = new HttpHeaders();
            //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            //  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            String body = stringResponseEntity.getBody();
            log.info(body);
            body=new String(body.getBytes("ISO-8859-1"),"utf-8");
            WechatSendMsgDto result= JSON.parseObject(body, WechatSendMsgDto.class);
            if(null!=result&&"40001".equals(result.getErrcode())){
                 access_token = getAccess_token(true);
                 url = SEND_URL.replace("{access_token}", access_token);
                failUsers.add(user);
            }
        }

        //重试
        for (MsgUser user : failUsers) {
            if(null==user||null==user.getOpenid()){
                continue;
            }
            Map<String,Object> map= Maps.newHashMap();
            map.put("touser",user.getOpenid());
            map.put("template_id","sLEqXwLwKZB1RWZDqA9JHTumJawA_l6uuQlbE100v_c");
            map.put("miniprogram",MapUtil.of("appid","wxf944787a26c46ba5","pagepath","pages/userInfo/index?notice=true"));
            Map data= MapUtil.of("first",MapUtil.of("value",user.getNickname()+"，您关注的2019国考报名入口即将开启","color","#173177"),
                    "keyword1",MapUtil.of("value","2019年国家公务员考试报名\n报名时间：10月22日 8:00-10月31日18:00","color","#173177"),
                    "keyword2","",
                    "remark",MapUtil.of("value","华图在线 [公务员职位库] 为您保留了意向职位，快去查看。","color","#173177"));
            map.put("data",data);
            String requestBody = JSONObject.toJSONString(map);
            HttpHeaders headers = new HttpHeaders();
            //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            //  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            String body = stringResponseEntity.getBody();
            log.info(body);
            body=new String(body.getBytes("ISO-8859-1"),"utf-8");
            WechatSendMsgDto result= JSON.parseObject(body, WechatSendMsgDto.class);
            if(null!=result&&"40001".equals(result.getErrcode())){
                access_token = getAccess_token(true);
                url = SEND_URL.replace("{access_token}", access_token);
            }
        }
    }

}
