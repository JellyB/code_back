package com.huatu.tiku.interview.controller.api.v1;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.MobileService;
import com.huatu.tiku.interview.service.UserService;
import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-05 下午5:36
 **/

//@CrossOrigin
@Slf4j
@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private MobileService mobileService;

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    WechatTemplateMsgService templateMsgService;
    //推送模板消息
    @Value("${notify_view}")
    private String notifyView;


    /**
     * 保存用户信息（关注后，绑定手机号的相关校验+发送验证码）
     * @param mobile
     * @param openId
     * @param req
     * @return
     */
    @LogPrint
    @GetMapping("getMobile")
    public Result getMobile(String mobile, String openId, HttpServletRequest req) {

        log.debug("用户openId:"+openId);
        return Result.ok(mobileService.checkPHP(mobile, openId, req));
    }


    /**
     * 检验手机号验证码
     * @param mobile
     * @param captcha
     * @param request
     * @return
     */
    @LogPrint
    @GetMapping(value = "getNext")
    public Result userCaptcha(String mobile, String captcha,HttpServletRequest request) {

        return  mobileService.userCaptcha(mobile, captcha,request);

    }
//    @PutMapping
//    public void updateUserInfo(@RequestBody User user,HttpServletRequest request){
//        log.info("id:{}",user.getId());
//        if(user==null || user.getId()==0){
//            throw new BizException(ErrorResult.create(403,"参数有误"));
//        }
//        userService.updateUser(user,request);
//    }


    //    @PostMapping
//    public void createUser(@RequestBody String openId){
//        log.info("id:{}",openId);
//        if(StringUtils.isBlank(openId)){
//            throw new ReqException(ErrorResult.create(403,"参数有误"));
//        }
//        userService.createUser(openId);
//    }

    /**
     * 完善用户信息
     * @param user
     * @param request
     * @return
     */
    @LogPrint
    @PostMapping
    public Result updateUser(@RequestBody User user, HttpServletRequest request) {

        if (user.getAgreement() == null){
            user.setAgreement(false);
        }
        if(!user.getAgreement()){
            return Result.build(ResultEnum.Agreement_ERROR);
        }
        return userService.updateUser(user, request) ? Result.ok() : Result.build(ResultEnum.UPDATE_FAIL);
    }

    /**
     * 查询用户信息
     * @param openId
     * @return
     */
    @LogPrint
    @GetMapping
    public Result getUserInfo(String openId) {
        User user = userService.getUser(openId);
        if (user == null){
            user = new User();
            user.setKeyContact("");
            user.setNation("");
            user.setIdCard("");
            user.setName("");
            user.setSex(-1);
            user.setPhone("");
            user.setStatus(-1);
            user.setPregnancy(null);
            user.setAgreement(false);

        }
        return Result.ok(user);
    }

//    /**
//     * 推送消息????
//     * @return
//     */
//    @LogPrint
//    @GetMapping("pushNotify")
//    public Result pushNotify(){
//        String accessToken = redis.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
//        WechatTemplateMsg templateMsg ;
//        for (User u : userService.findAllUser()) {
//            templateMsg = new WechatTemplateMsg(u.getOpenId(), TemplateEnum.MorningReading);
//            templateMsg.setUrl(notifyView+6);
//            templateMsg.setData(
//                    MyTreeMap.createMap(
//                            new TemplateMap("first", WechatTemplateMsg.item("今日热点已新鲜出炉~", "#000000")),
//                            new TemplateMap("keyword1", WechatTemplateMsg.item(u.getName(), "#000000")),
//                            new TemplateMap("keyword2", WechatTemplateMsg.item("123", "#000000")),
//                            new TemplateMap("remark", WechatTemplateMsg.item("华图在线祝您顺利上岸！", "#000000"))
//                    )
//            );
//            templateMsgService.sendTemplate(accessToken, JsonUtil.toJson(templateMsg));
//
//        }
//        for (User u : userService.findAllUser()) {
//            templateMsg = new WechatTemplateMsg(u.getOpenId(), TemplateEnum.MorningReading);
//            templateMsg.setUrl(notifyView+6);
//            templateMsg.setData(
//                    MyTreeMap.createMap(
//                            new TemplateMap("first", WechatTemplateMsg.item("今日热点已新鲜出炉~", "#000000")),
//                            new TemplateMap("keyword1", WechatTemplateMsg.item(u.getName(), "#000000")),
//                            new TemplateMap("keyword2", WechatTemplateMsg.item("1233", "#000000")),
//                            new TemplateMap("remark", WechatTemplateMsg.item("华图在线祝您顺利上岸！", "#000000"))
//                    )
//            );
//            templateMsgService.sendTemplate(accessToken, JsonUtil.toJson(templateMsg));
//
//        }
//        return Result.ok();
//    }



}
