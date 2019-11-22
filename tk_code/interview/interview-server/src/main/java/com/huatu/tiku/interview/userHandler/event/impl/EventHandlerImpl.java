package com.huatu.tiku.interview.userHandler.event.impl;

import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.interview.constant.BasicParameters;
import com.huatu.tiku.interview.constant.NotificationTypeConstant;
import com.huatu.tiku.interview.constant.UserStatusConstant;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.Article;
import com.huatu.tiku.interview.entity.message.NewsMessage;
import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.service.UserService;
import com.huatu.tiku.interview.userHandler.event.EventHandler;
import com.huatu.tiku.interview.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/12 10:07
 * @Modefied By:
 * 用户事件处理类
 */
@Component
@Slf4j
public class EventHandlerImpl implements EventHandler {

    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SignInRepository signInRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationTypeRepository notificationTypeRepository;
    @Value("${phone_check}")
    private String phoneCheck;
    @Autowired
    private UserClassRelationRepository userClassRelationRepository;
    private static final String SIGN_SUCC_MSG = "恭喜，您于%s签到成功。";
    @Value("${signKey}")
    private String signKey;
    @Value("${ScanResult}")
    private String ScanResult;


    /**
     * 关注
     * @param requestMap
     * @return
     */
    @Override
    public String subscribeHandler(Map<String, String> requestMap) {
        String fromUserName = requestMap.get("FromUserName");
        User user = userService.getUserByOpenId(fromUserName);
        if (user == null) {
            userService.createUser(fromUserName);
        }else{
            user.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            userRepository.save(user);
        }
        NewsMessage nm = new NewsMessage(requestMap);
        List<Article> as = new ArrayList<>();
        Article a = new Article();
        a.setTitle("点击消息，完成手机绑定");
        a.setDescription("为了保证您的课堂体验与学习效果，请您务必完成手机绑定，同时完善个人信息。");
        a.setPicUrl(BasicParameters.IMAGE_SUBSCRIBE_001);
        //这里跳转前端验证
        a.setUrl(phoneCheck + "openId=" + fromUserName);
        as.add(a);
        nm.setArticleCount(as.size());
        nm.setArticles(as);
        return MessageUtil.MessageToXml(nm);
    }


    /**
     * 取关
     * @param requestMap
     * @return
     */
    @Override
    public String unSubscribeHandler(Map<String, String> requestMap) {
        User user = userService.getUserByOpenId(requestMap.get("FromUserName"));
        user.setPhone("");
        user.setBizStatus(UserStatusConstant.BizStatus.UN_BIND.getBizSatus());
        user.setStatus(WXStatusEnum.Status.DELETE.getStatus());
        userService.save(user);
        return "取关成功";
    }

    /**
     * 二维码签到
     * @param requestMap
     * @return
     */
    @Override
    public String signInHandler(Map<String, String> requestMap) {
        String str;
        SignIn signIn = new SignIn();
        signIn.setOpenId(requestMap.get("FromUserName"));
        signIn.setSignTime(new Date());
        signIn.setStatus(1);

        if (("sign_in".equals(requestMap.get("EventKey"))) && ScanResult.equals(requestMap.get("ScanResult"))
                ||signKey.equals(requestMap.get("Ticket"))) {
            log.info("二维码为华图官方签到二维码，开始校验用户信息");
            String current = DateFormatUtil.NORMAL_TIME_FORMAT.format(new Date());
            boolean flag = false;
            User user = userRepository.findByOpenId(requestMap.get("FromUserName"));
            if ((user != null && user.getStatus() == 1)) {
                //查询用户所在班级
                Long classId = 0L;
                List<UserClassRelation> userClassRelationList = userClassRelationRepository.findByOpenIdAndStatus(user.getOpenId(), WXStatusEnum.Status.NORMAL.getStatus());
                if(CollectionUtils.isNotEmpty(userClassRelationList)){
                    classId = userClassRelationList.get(0).getClassId();
                }

                ClassInfo classInfo = classInfoRepository.findOne(classId);
                if(CollectionUtils.isEmpty(userClassRelationList) || null == classInfo){
                    log.info("签到失败");
                    str = WxMpXmlOutMessage
                            .TEXT()
                            .content("未查询到您的班级信息，若有疑问，请联系客服：400-817-6111")
                            .fromUser(requestMap.get("ToUserName"))
                            .toUser(requestMap.get("FromUserName"))
                            .build().toXml();
                    signIn.setBizStatus(5);
                    signInRepository.save(signIn);
                    return str;
                }
                //获取签到时间信息  签到时间：早上：8:00—9:00中午：13:30——14:30晚上：18:30——19:00
                String signInTime = classInfo.getSignInTime();
                String[] split = signInTime.split(",");
                if(split.length != 0){
                    for(String time:split){
                        String[] times = time.split("-");
                        String startTime = times[0];
                        String endTime = times[1];
                        if(current.compareTo(startTime)>= 0 &&  current.compareTo(endTime) <= 0){
                            flag = true;
                            break;
                        }
                    }
                }

                if (flag) {
                    log.info("开始签到");
                    String time = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date());
                    String timeStr = String.format(SIGN_SUCC_MSG, time);
                    str = WxMpXmlOutMessage
                            .TEXT()
                            .content(timeStr)
                            .fromUser(requestMap.get("ToUserName"))
                            .toUser(requestMap.get("FromUserName"))
                            .build()
                            .toXml();

                    signIn.setBizStatus(1);
                    signInRepository.save(signIn);

                } else {
                    log.info("签到失败");
                    str = WxMpXmlOutMessage
                            .TEXT()
                            .content("请在规定时间内签到！")
                            .fromUser(requestMap.get("ToUserName"))
                            .toUser(requestMap.get("FromUserName"))
                            .build().toXml();
                    signIn.setBizStatus(2);
                    signInRepository.save(signIn);
                    return str;
                }
            } else {
                log.info("用户未购买相关课程");
                str = WxMpXmlOutMessage
                        .TEXT()
                        .content("签到失败")
                        .fromUser(requestMap.get("ToUserName"))
                        .toUser(requestMap.get("FromUserName"))
                        .build().toXml();
                signIn.setBizStatus(3);
                signInRepository.save(signIn);
                return str;
            }
        } else {
            log.info("非签到二维码");
            str = WxMpXmlOutMessage
                    .TEXT()
                    .content("该二维码不可用于签到")
                    .fromUser(requestMap.get("ToUserName"))
                    .toUser(requestMap.get("FromUserName"))
                    .build().toXml();
            signIn.setBizStatus(4);
            signInRepository.save(signIn);
            return str;
        }
        return str;
    }

    /**
     * 处理点击事件
     *
     * @param requestMap
     * @return
     */
    @Override
    public String eventClick(Map<String, String> requestMap) {
        String str = null;
        if ("course".equals(requestMap.get("EventKey"))) {
            User user = userRepository.findByOpenId(requestMap.get("FromUserName"));
            if ((user == null || user.getBizStatus() != 2)) {
                log.info("----查询不到用户信息----");
                str = WxMpXmlOutMessage
                        .TEXT()
                        .content("抱歉，只有已购买《面试封闭集训营》课程的学员才能享有该服务。若有疑问，请联系客服：400-817-6111")
                        .fromUser(requestMap.get("ToUserName"))
                        .toUser(requestMap.get("FromUserName"))
                        .build()
                        .toXml();
            } else {
                // 查询用户所属班级
                long classId = 0;
                //查询用户所属班级
                List<UserClassRelation> userClassRelationList = userClassRelationRepository.findByOpenIdAndStatus(user.getOpenId(), WXStatusEnum.Status.NORMAL.getStatus());
                if(CollectionUtils.isEmpty(userClassRelationList)){
                    log.info("用户没有所属班级");
                }else{
                    UserClassRelation userClassRelation = userClassRelationList.get(0);
                    classId = userClassRelation.getClassId();
                }
                // 查询用户所属班级的课表图片
                List<NotificationType> imageList = notificationTypeRepository.findByTypeAndClassIdAndStatusOrderByGmtCreateDesc(NotificationTypeConstant.ONLINE_COURSE_ARRANGEMENT.getCode(), classId,WXStatusEnum.Status.NORMAL.getStatus() );

//                List<NotificationType> notTypePatterns = notificationTypeRepository.findByBizStatusAndStatus
//                        (new Sort(Sort.Direction.DESC, "gmtModify"), WXStatusEnum.BizStatus.ONLINE.getBizSatus(), WXStatusEnum.Status.NORMAL.getStatus());
                if(CollectionUtils.isNotEmpty(imageList)){
                    for (NotificationType notificationType : imageList) {
                        if (StringUtils.isNotEmpty(notificationType.getWxImageId())) {
                            log.info("----展示图片----");
                            log.info("----图片id:" + notificationType.getWxImageId());
                            str = WxMpXmlOutMessage
                                    .IMAGE()
                                    .mediaId(notificationType.getWxImageId())
                                    .fromUser(requestMap.get("ToUserName"))
                                    .toUser(requestMap.get("FromUserName"))
                                    .build()
                                    .toXml();
                            break;
                        }
                    }
                }else{
                    str = WxMpXmlOutMessage
                            .TEXT()
                            .content("课程安排即将推出~")
                            .fromUser(requestMap.get("ToUserName"))
                            .toUser(requestMap.get("FromUserName"))
                            .build()
                            .toXml();

                }
            }
        } else if ("conn_service".equals(requestMap.get("EventKey"))) {
            str = WxMpXmlOutMessage
                    .TEXT()
                    .content("客服电话：400-817-6111")
                    .fromUser(requestMap.get("ToUserName"))
                    .toUser(requestMap.get("FromUserName"))
                    .build()
                    .toXml();
        } else {
            str = WxMpXmlOutMessage
                    .TEXT()
                    .content("正在开发")
                    .fromUser(requestMap.get("ToUserName"))
                    .toUser(requestMap.get("FromUserName"))
                    .build()
                    .toXml();
        }
        log.info("----返回的xml:" + str);
        return str;
    }


    /**
     * 二维码签到(不限制开始结束时间)
     * @param requestMap
     * @return
     */
    @Override
    public String signInHandlerV2(Map<String, String> requestMap) {
        String str;
        SignIn signIn = new SignIn();
        signIn.setOpenId(requestMap.get("FromUserName"));
        signIn.setSignTime(new Date());
        signIn.setStatus(1);

        if (("sign_in".equals(requestMap.get("EventKey"))) && ScanResult.equals(requestMap.get("ScanResult"))
                ||signKey.equals(requestMap.get("Ticket"))) {
            log.info("二维码为华图官方签到二维码，开始校验用户信息");
            String current = DateFormatUtil.NORMAL_TIME_FORMAT.format(new Date());
            User user = userRepository.findByOpenId(requestMap.get("FromUserName"));
            if ((user != null && user.getStatus() == 1)) {
                //查询用户所在班级
                Long classId = 0L;
                List<UserClassRelation> userClassRelationList = userClassRelationRepository.findByOpenIdAndStatus(user.getOpenId(), WXStatusEnum.Status.NORMAL.getStatus());
                if(CollectionUtils.isNotEmpty(userClassRelationList)){
                    classId = userClassRelationList.get(0).getClassId();
                }

                ClassInfo classInfo = classInfoRepository.findOne(classId);
                if(CollectionUtils.isEmpty(userClassRelationList) || null == classInfo){
                    log.warn("签到失败，未查询到学员的班级信息，openId：{}",requestMap.get("FromUserName"));
                    str = WxMpXmlOutMessage
                            .TEXT()
                            .content("未查询到您的班级信息，若有疑问，请联系客服：400-817-6111")
                            .fromUser(requestMap.get("ToUserName"))
                            .toUser(requestMap.get("FromUserName"))
                            .build().toXml();
                    signIn.setBizStatus(5);
                    signInRepository.save(signIn);
                    return str;
                }
                log.info("用户信息校验通过，开始签到");
                String time = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date());
                String timeStr = String.format(SIGN_SUCC_MSG, time);
                str = WxMpXmlOutMessage
                        .TEXT()
                        .content(timeStr)
                        .fromUser(requestMap.get("ToUserName"))
                        .toUser(requestMap.get("FromUserName"))
                        .build()
                        .toXml();

                signIn.setBizStatus(1);
                signInRepository.save(signIn);


            } else {
                log.info("用户未购买相关课程");
                str = WxMpXmlOutMessage
                        .TEXT()
                        .content("签到失败")
                        .fromUser(requestMap.get("ToUserName"))
                        .toUser(requestMap.get("FromUserName"))
                        .build().toXml();
                signIn.setBizStatus(3);
                signInRepository.save(signIn);
                return str;
            }
        } else {
            log.info("非签到二维码");
            str = WxMpXmlOutMessage
                    .TEXT()
                    .content("该二维码不可用于签到")
                    .fromUser(requestMap.get("ToUserName"))
                    .toUser(requestMap.get("FromUserName"))
                    .build().toXml();
            signIn.setBizStatus(4);
            signInRepository.save(signIn);
            return str;
        }
        return str;
    }



}
