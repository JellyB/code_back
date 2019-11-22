package com.huatu.tiku.interview.service.impl;

import com.google.gson.JsonParser;
import com.huatu.tiku.interview.userHandler.event.EventHandler;
import com.huatu.tiku.interview.userHandler.message.MessageHandler;
import com.huatu.tiku.interview.service.CoreService;
import com.huatu.tiku.interview.util.MessageUtil;
import com.huatu.tiku.interview.util.WeiXinAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhouwei
 * @Description: 核心服务类
 * @create 2018-01-04 下午12:09
 **/
@Service
@Slf4j
//@PropertySource(value = {"classpath:application.yml"},
//ignoreResourceNotFound = true,encoding = "utf-8")
public class CoreServiceImpl implements CoreService {

    private static final JsonParser JSON_PARSER = new JsonParser();

    @Autowired
    private EventHandler eventHandler;

    @Autowired
    private MessageHandler messageHandler;


    @Autowired
    private WeiXinAccessTokenUtil accessTokenUtil;


    @Autowired
    private ServletContext servletContext;

    /**
     * 处理微信发来的请求（包括事件的推送）
     *
     * @param request
     * @return
     */

    @Override
    public String processRequest(Map<String, String> requestMap, HttpServletRequest request, HttpServletResponse response) {
        String result = null;
        try {
            requestMap.forEach((k, v) -> {
                log.info("key:{},value:{}", k, v);
            });
            switch (requestMap.get("MsgType")) {
                //文本消息
                case MessageUtil.REQ_MESSAGE_TYPE_TEXT: {
                    result = messageHandler.TextMessageHandler(requestMap);
                }   //图片消息
                case MessageUtil.REQ_MESSAGE_TYPE_IMAGE: {
                    break;
                }   //链接消息
                case MessageUtil.REQ_MESSAGE_TYPE_LINK: {
                    break;
                }   //地理消息
                case MessageUtil.REQ_MESSAGE_TYPE_LOCATION: {
                    break;
                }   //音频消息
                case MessageUtil.REQ_MESSAGE_TYPE_VOICE: {
                    break;
                }   //事件
                case MessageUtil.REQ_MESSAGE_TYPE_EVENT: {
                    switch (requestMap.get("Event")) {
                        //自定义菜单点击事件
                        case MessageUtil.EVENT_TYPE_CLICK: {
                            result = eventHandler.eventClick(requestMap);
                            break;
                        }   // 订阅（关注）时的处理
                        case MessageUtil.EVENT_TYPE_SUBSCRIBE: {
                            result = eventHandler.subscribeHandler(requestMap);
                            break;
                        }   //取关
                        case MessageUtil.EVENT_TYPE_UNSUBSCRIBE: {
                            result = eventHandler.unSubscribeHandler(requestMap);
                            break;
                        }   //万恶之源
                        case MessageUtil.TEMPLATESENDJOBFINISH: {
                            // 模板验证消息
                            break;
                        }//扫一扫签到
                        case MessageUtil.SCANCODE_WAITMSG: {
                            result = eventHandler.signInHandlerV2(requestMap);
//                            System.out.println("拦截的是注释过的case");
                            break;
                        }
                        case MessageUtil.SCANCODE_SCAN: {
                            result = eventHandler.signInHandlerV2(requestMap);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

//    public String processRequest(HttpServletRequest request) {
//
//        String respMessage = null;
//        try {
//            // 默认返回的文本消息内容
//            String respContent = "请求处理异常，请稍候尝试！";
//            // xml请求解析
//            Map<String, String> requestMap = MessageUtil.parseXml(request);
//            requestMap.forEach((k,v)->{
//                log.info("key:{},value:{}",k,v);
//            });
//            // 发送方帐号（open_id）
//            String fromUserName = requestMap.get("FromUserName");
//            // 公众帐号
//            String toUserName = requestMap.get("ToUserName");
//            // 消息类型
//            String msgType = requestMap.get("MsgType");
//            // 回复文本消息
//            TextToMessage textMessage = new TextToMessage();
//            textMessage.setToUserName(fromUserName);
//            textMessage.setFromUserName(toUserName);
//            textMessage.setCreateTime(new Date().getTime());
//            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
//            textMessage.setFuncFlag(0);
//
//
//            // 创建图文消息
//            NewsToMessage newsMessage = new NewsToMessage();
//            newsMessage.setToUserName(fromUserName);
//            newsMessage.setFromUserName(toUserName);
//            newsMessage.setCreateTime(new Date().getTime());
//            newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
//            newsMessage.setFuncFlag(0);
//
//            List<Article> articleList = new ArrayList<Article>();
//
//            //点击菜单id
//            String EventKey = requestMap.get("EventKey");
//            // 接收文本消息内容
//            String content = requestMap.get("Content");
//            // 自动回复文本消息
//            if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
//
//                //如果用户发送表情，则回复同样表情。
//                if (isQqFace(content)) {
//                    respContent = content;
//                    textMessage.setContent(respContent);
//                    // 将文本消息对象转换成xml字符串
//                    respMessage = MessageUtil.textMessageToXml(textMessage);
//                } else {
//                    System.out.println("文本内容是："+content);
//                    // 测试的判断-----------------------------------------------------------
//                    if(content.equals("mobile")){
//                        //测试单图文回复
//                        Article article = new Article();
//                        article.setTitle("谢谢您的关注！");
//                        // 图文消息中可以使用QQ表情、符号表情
//                        article.setDescription("点击图文可以跳转到华图首页");
//                        // 将图片置为空
//                        article.setPicUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1515474563395&di=7fd2315a708740c4b8ed01b68ff8d1d4&imgtype=0&src=http%3A%2F%2Fwww.zhlzw.com%2FUploadFiles%2FArticle_UploadFiles%2F201204%2F20120412123904521.jpg");
//                        article.setUrl("http://60.205.179.253");
//                        articleList.add(article);
//                        newsMessage.setArticleCount(articleList.size());
//                        newsMessage.setArticles(articleList);
//                        respMessage = MessageUtil.newsMessageToXml(newsMessage);
//                        return respMessage;
//                    }else{
//                    // -----------------------------------------------------------
//                        textMessage.setContent(content);
//                        // 将文本消息对象转换成xml字符串
//                        respMessage = MessageUtil.textMessageToXml(textMessage);
//                    }
//
//                }
//            }
//            // 图片消息
//            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
//                respContent = "您发送的是图片消息！";
//                textMessage.setContent(respContent);
//                // 将文本消息对象转换成xml字符串
//                respMessage = MessageUtil.textMessageToXml(textMessage);
//            }
//            // 地理位置消息
//            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
//                respContent = "您发送的是地理位置消息！";
//                textMessage.setContent(respContent);
//                // 将文本消息对象转换成xml字符串
//                respMessage = MessageUtil.textMessageToXml(textMessage);
//            }
//            // 链接消息
//            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
//                respContent = "您发送的是链接消息！";
//                textMessage.setContent(respContent);
//                // 将文本消息对象转换成xml字符串
//                respMessage = MessageUtil.textMessageToXml(textMessage);
//
//            }
//            // 音频消息
//            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
//                respContent = "您发送的是音频消息！";
//                textMessage.setContent(respContent);
//                // 将文本消息对象转换成xml字符串
//                respMessage = MessageUtil.textMessageToXml(textMessage);
//            }
//
//
//            // 事件推送
//            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
//                // 事件类型
//                String eventType = requestMap.get("Event");
//                // 自定义菜单点击事件
//                if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
//                    switch (EventKey) {
//                        case "m_imgmsg": {
//                            log.info("sign:"+fromUserName);
//                            respContent = "恭喜，签到成功";
//                            break;
//                        }
//
//                        default: {
//                            log.error("开发者反馈：EventKey值没找到，它是:" + EventKey);
//                            respContent = "程序员玩命开发中。。。";
//                        }
//                    }
//                    textMessage.setContent(respContent);
//                    // 将文本消息对象转换成xml字符串
//                    respMessage = MessageUtil.textMessageToXml(textMessage);
//                } else if (eventType.equals(MessageUtil.EVENT_TYPE_VIEW)) {
//                    // 对于点击菜单转网页暂时不做推送
//                }
//
//                // 订阅
//                else if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
//                    respMessage = eventHandler.subscribeEvent(fromUserName ,newsMessage, respMessage);
//                } else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
//                    //测试单图文回复
//                    Article article = new Article();
//                    article.setTitle("这是已关注用户扫描二维码弹到的界面");
//                    // 图文消息中可以使用QQ表情、符号表情
//                    article.setDescription("点击图文可以跳转到百度首页");
//                    // 将图片置为空
//                    article.setPicUrl("http://www.sinaimg.cn/dy/slidenews/31_img/2016_38/28380_733695_698372.jpg");
//                    article.setUrl("http://www.baidu.com");
//                    articleList.add(article);
//                    newsMessage.setArticleCount(articleList.size());
//                    newsMessage.setArticles(articleList);
//                    respMessage = MessageUtil.newsMessageToXml(newsMessage);
//                }
//                // 取消订阅
//                else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
//                    // TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return respMessage;
//    }

    /**
     * 判断是否是QQ表情
     *
     * @param content
     * @return
     */
    public static boolean isQqFace(String content) {
        boolean result = false;

        // 判断QQ表情的正则表达式
        String qqfaceRegex = "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::$|/::X|/::Z|/::'\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::!|/::L|/::>|/::,@|/:,@f|/::-S|/:\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap|/:&-\\(|/:B-\\)|/:<@|/:@>|/::-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pig|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/:footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|/:<L>|/:jump|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY|/:#-0|/:hiphot|/:kiss|/:<&|/:&>";
        Pattern p = Pattern.compile(qqfaceRegex);
        Matcher m = p.matcher(content);
        if (m.matches()) {
            result = true;
        }
        return result;
    }


}
