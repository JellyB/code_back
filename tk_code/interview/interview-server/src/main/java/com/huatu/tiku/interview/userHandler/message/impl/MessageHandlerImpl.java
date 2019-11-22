package com.huatu.tiku.interview.userHandler.message.impl;

import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.userHandler.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/12 9:56
 * @Modefied By:
 * 消息处理类
 */
@Component
@Slf4j
public class MessageHandlerImpl implements MessageHandler {

    @Autowired
    private WechatTemplateMsgService templateMsgService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private ServletContext servletContext;
    @Value("${notify_view}")
    private String notifyView;

    @Value("${phone_check}")
    private String phoneCheck;

    @Override
    public String TextMessageHandler(Map<String, String> requestMap){
//        String accessToken = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN_KEY);
////        accessToken = "6_xsXvmd5iX-22XaVXgCmlUzJ8V6YKssh7XfaVtZXe6GdzSydUPcKT8i4G2ULClF9Th9wmQQ9LUOGrZI8kxj330SApNk9HcEYSei1sD9F7daYj7Q3IryQQffHC9IMLIYgAGASPF";
//        System.out.println("accessToken:"+accessToken);

//        if(requestMap.get("Content").equals("1")){
//            //            String templateMsgJson = WechatTemplateMsg.getJson(TemplateEnum.No_2,requestMap);
//            WechatTemplateMsg templateMsg = new WechatTemplateMsg(TemplateEnum.HuaTu01,requestMap);
//
//            String templateMsgJson = JsonUtil.toJson(templateMsg);
//            TemplateMsgResult msgResult = templateMsgService.sendTemplate(
//                    accessToken,
//                    templateMsgJson);
//            return null;
//        }
//        if(requestMap.get("Content").equals("2")){
//            //这个是直接生成String
////            String templateMsgJson = WechatTemplateMsg.getJson(TemplateEnum.No_2,requestMap);
//            WechatTemplateMsg templateMsg = new WechatTemplateMsg(TemplateEnum.No_2,requestMap);
//
//            String templateMsgJson = JsonUtil.toJson(templateMsg);
//            TemplateMsgResult msgResult = templateMsgService.sendTemplate(
//                    accessToken,
//                    templateMsgJson);
//            return null;
//            NewsMessage nm = new NewsMessage(requestMap);
//            List<Article> as = new ArrayList<>();
//            Article a = new Article();
//            a.setTitle("呵呵！!");
//            a.setDescription("点击图文可以跳转到华图首页");
//            a.setPicUrl("http://p1.music.126.net/_mEC5ZpzngngbBioF8dm4Q==/109951162973202394.jpg");
//            //这里跳转前端验证
//            log.info("手机验证路径："+phoneCheck+requestMap.get("FromUserName"));
//            a.setUrl(phoneCheck + "openId="+requestMap.get("FromUserName"));
//            as.add(a);
//            nm.setArticleCount(as.size());
//            nm.setArticles(as);
//            return MessageUtil.MessageToXml(nm);
//        }
//        if(requestMap.get("Content").equals("3")){
//            WechatTemplateMsg templateMsg = new WechatTemplateMsg(requestMap.get("FromUserName"), TemplateEnum.MorningReading);
//                    templateMsg.setUrl(notifyView+6);
//                    templateMsg.setData(
//                            MyTreeMap.createMap(
//                                    new TemplateMap("first", WechatTemplateMsg.item("今日热点已新鲜出炉~", "#000000")),
//                                    new TemplateMap("keyword1", WechatTemplateMsg.item("不知道", "#000000")),
//                                    new TemplateMap("keyword2", WechatTemplateMsg.item("不知道", "#000000")),
//                                    new TemplateMap("remark", WechatTemplateMsg.item("华图在线祝您顺利上岸！", "#000000"))
//                            )
//                    );
//            templateMsgService.sendTemplate(accessToken, JsonUtil.toJson(templateMsg));
//            return null;
//        }
//        if(!requestMap.get("Content").equals("2")){
//            System.out.println("手机验证路径："+phoneCheck+requestMap.get("FromUserName"));
//            NewsMessage nm = new NewsMessage(requestMap);
//            List<Article> as = new ArrayList<>();
//            Article a = new Article();
//            a.setTitle("谢谢您的关注！!");
//            a.setDescription("点击图文可以跳转到华图首页");
//            a.setPicUrl(BasicParameters.IMAGE_SUBSCRIBE_001);
//            //这里跳转前端验证
//            a.setUrl(phoneCheck + "openId="+requestMap.get("FromUserName"));
//            as.add(a);
//            nm.setArticleCount(as.size());
//            nm.setArticles(as);
//            return MessageUtil.MessageToXml(nm);
//        }
        // TODO 这里还要做特定字符验证，以及正则验证
//        TextMessage tm = new TextMessage("暂无验证",requestMap);
//        return MessageUtil.MessageToXml(tm);
        return null;
    }
    @Override
    public String ImageMessageHandler(Map<String, String> requestMap) {
        return null;
    }
}

