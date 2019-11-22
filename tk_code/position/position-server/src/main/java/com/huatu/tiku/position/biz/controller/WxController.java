package com.huatu.tiku.position.biz.controller;

import cn.binarywang.wx.miniapp.util.crypt.WxMaCryptUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.huatu.tiku.position.base.exception.BadRequestException;
import com.huatu.tiku.position.base.exception.NoAuthException;
import com.huatu.tiku.position.base.exception.NoLoginException;
import com.huatu.tiku.position.biz.domain.User;
import com.huatu.tiku.position.biz.dto.LoginDto;
import com.huatu.tiku.position.biz.dto.WeChatAccessTokenDto;
import com.huatu.tiku.position.biz.dto.WeChatResponseDto;
import com.huatu.tiku.position.biz.dto.WxDecryptDto;
import com.huatu.tiku.position.biz.enums.Status;
import com.huatu.tiku.position.biz.service.UserService;
import com.huatu.tiku.position.biz.util.RedisUtil;
import com.huatu.tiku.position.biz.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author wangjian
 **/
@Slf4j
@RestController
@RequestMapping("weChat")
public class WxController {

    @Value("${api.url}")
    private String AUTH_URL;

    @Value("${api.access}")
    private String ACCESS_URL;

    private static final String ACCESS_TOKEN_KEY="positionAccessToken:";

    private RestTemplate restTemplate;

    private UserService userService;

    private RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WxController(RestTemplate restTemplate, UserService userService, RedisUtil redisUtil) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.redisUtil = redisUtil;
    }

    /**
     * 获取微信getAccessToken
     */
    @GetMapping("getAccessToken")
    public String getAccessToken() throws  Exception {
        String access_token = redisUtil.get(ACCESS_TOKEN_KEY);
        if(StringUtils.isNotBlank(access_token)){
            Long expire = redisUtil.getExpire(ACCESS_TOKEN_KEY);
            return objectMapper.writeValueAsString(ImmutableMap.of("access_token", access_token,"expires_in",expire));
        }else {
            String url = ACCESS_URL;
            ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
            String body = forEntity.getBody();
            log.info(body);
            WeChatAccessTokenDto dto = JSON.parseObject(body, WeChatAccessTokenDto.class);
            access_token = dto.getAccess_token();
            if (StringUtils.isBlank(access_token)) {  //缓存
                throw new BadRequestException("access_token获取失败");
            }
            redisUtil.set(ACCESS_TOKEN_KEY, access_token, Long.valueOf(dto.getExpires_in()));
            return body;
        }
    }


    /**
     * 获取微信openId
     */
    @GetMapping("getOpenId")
    public Map getOpenId(String code) {
        if(StringUtils.isBlank(code)){
            throw new BadRequestException("code错误");
        }
        String url = AUTH_URL.replace("{code}", code);
        log.info(url);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        log.info(body);
        WeChatResponseDto weChatResponseDto = JSON.parseObject(body, WeChatResponseDto.class);
        String openId = weChatResponseDto.getOpenid();
        if(StringUtils.isBlank(openId)){  //没有openId
            throw new BadRequestException("code登录失败");
        }else {   //有openId
            User user;
            try {
                user = userService.findByOpenId(openId);
            } catch (NoLoginException e) {//没有用户 注册用户
                user=new User();
                user.setOpenId(openId);
                user.setStatus(Status.ZC);
                if (StringUtils.isNotBlank(weChatResponseDto.getUnionid())) {  //有unionid情况
                    user.setUnionId(weChatResponseDto.getUnionid());
                }
                user=userService.save(user);
            }
            if(null==user.getUnionId()&&StringUtils.isNotBlank(weChatResponseDto.getUnionid())){
                userService.updateUnionidByOpenId(weChatResponseDto.getUnionid(), openId);
            }
        }
        return ImmutableMap.of("openId", openId);
    }

    /**
     * 获取验证码
     */
    @GetMapping("getCode")
    public Map<String, String> getCode( String phone){
        String regEx = "^1[2|3|4|5|6|7|8|9]\\d{9}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(phone);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        if (!rs) {
            throw new BadRequestException("手机号格式错误");
        }
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<4;i++){//4位随机数
            sb.append(r.nextInt(10));
        }
        final String code = sb.toString();
        redisUtil.set("positionCode:"+phone,code,600L);
        SmsUtil.sendSms(phone,"职位库小程序注册/登录验证码:"+code+"十分钟内有效");
        log.info("sendsms : {}->{}", phone, code);
        return ImmutableMap.of("phone", phone,"scucces","true");
    }

    /**
     * 通过手机验证码的登录
     */
    @PostMapping("login")
    public Map login(@Valid @RequestBody LoginDto dto, BindingResult bindingResult,
                      @RequestHeader String openId){
        if(StringUtils.isBlank(openId)){
            throw new NoAuthException("请授权");
        }
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String phone=dto.getPhone();
        String codeString = redisUtil.get("positionCode:"+phone);//验证码
        if(!StringUtils.isNotBlank(codeString)){
            throw new BadRequestException("请接收验证码");
        }
        log.info("phone:{} ,code : {}",phone, codeString);
        log.info("user phone:{} ,code : {}", phone,dto.getCode());
        if (!dto.getCode().equals(codeString)) {
            throw new BadRequestException("验证码错误");
        }
        redisUtil.remove(phone);
        User user = null;
        try {
            user = userService.findByOpenId(openId);
        } catch (NoLoginException e) {
//            user=new User();
//            user.setOpenId(openId);
//            user.setStatus(Status.ZC);
//            user.setPhone(phone);
//            user=userService.save(user);
            throw new BadRequestException("openId exception");
        }
        assert user != null;
        if(!phone.equals(user.getPhone())){  //两次手机号不一样
            user.setPhone(phone);
            userService.save(user);
        }
        return ImmutableMap.of("success", true);
    }

    /**
     * 解密微信授权获取手机号 根据openId创建/登录
     */
    @GetMapping("Decrypt")
    public Map Decrypt(String encryptedData,String iv,String code) throws Exception {
        String url = AUTH_URL.replace("{code}", code);
        log.info(url);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        log.info(body);
        WeChatResponseDto weChatResponseDto = JSON.parseObject(body, WeChatResponseDto.class);
        String sessionKey = weChatResponseDto.getSession_key();
        String openId = weChatResponseDto.getOpenid();
        if(!StringUtils.isNotBlank(sessionKey)||!StringUtils.isNotBlank(openId)){
            throw new BadRequestException("code登录失败");
        }
        encryptedData=URLDecoder.decode(encryptedData,"UTF-8");
        String resultString = WxMaCryptUtils.decrypt(sessionKey, encryptedData, iv);
        log.info(resultString);
        WxDecryptDto wxDecryptDto = JSON.parseObject(resultString, WxDecryptDto.class);
        String purePhoneNumber = wxDecryptDto.getPurePhoneNumber();
        User user = null;
        try {
            user = userService.findByOpenId(openId);
        } catch (NoLoginException e) {
//            user=new User();
//            user.setOpenId(openId);
//            user.setStatus(Status.ZC);
//            user.setPhone(purePhoneNumber);
//            user=userService.save(user);
            throw new BadRequestException("openId exception");
        }
        assert user != null;
        if(!purePhoneNumber.equals(user.getPhone())){  //两次手机号不一样
            user.setPhone(purePhoneNumber);
            userService.save(user);
        }
        return ImmutableMap.of("purePhoneNumber", wxDecryptDto.getPurePhoneNumber());
    }

    @GetMapping("wechatMsg")
    public void wechatMsga(HttpServletResponse response, String echostr) throws IOException {
        OutputStream os = response.getOutputStream();
        try {
            log.info("echostr->{}",echostr);
            if(StringUtils.isNotBlank(echostr)) {
                os.write(echostr.getBytes());
            }
        } finally {
            os.flush();
            os.close();
        }
    }

    @PostMapping("wechatMsg")
    public void wechatMsg(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        InputStream in = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in,
                "UTF-8"));
        StringBuffer buffers = new StringBuffer();
        String buffer = "";
        for (buffer = br.readLine(); buffer != null; buffer = br.readLine()) {
            buffers.append(buffer);
        }
        String retStr = buffers.toString();
        log.info("retStr:{}",retStr);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/xml; charset=UTF-8"));
        HttpEntity<String> httpEntity = new HttpEntity<>(retStr, headers);
        String url = "http://bwx3.ntalker.com/agent/weixin";
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
//        String body = stringResponseEntity.getBody();
//////        log.info(body);
        OutputStream os = response.getOutputStream();
        try {
            os.write("success".getBytes());
        } finally {
            os.flush();
            os.close();
        }
    }

}
