package com.huatu.ztk.user.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.user.bean.UserMessage;
import com.huatu.ztk.user.service.UserMessageService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人消息控制层
 * Created by shaojieyue
 * Created time 2016-06-16 20:10
 */

@RestController
@RequestMapping(value = "/v1/users/messages")
public class UserMessageControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserMessageControllerV1.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserMessageService userMessageService;

    /**
     * 查询用户消息列表
     * @param token
     * @param cursor 游标
     * @param size 请求的消息个数
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestHeader(required = false) String token,
                       @RequestParam long cursor,
                       @RequestParam(defaultValue = "-1") int categoryId,
                       @RequestHeader(defaultValue = "-1") int category,
                       @RequestParam(defaultValue = "20") int size) throws BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        //科目
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);
//        PageBean<UserMessage> pageBean = userMessageService.findMessage(userId, cursor, size, catgory);
        PageBean<UserMessage> pageBean = userMessageService.findMessageV2(userId, cursor, size, catgory);

        return pageBean;
    }

    /**
     * 查看消息详情
     * @param token
     * @param mid
     * @return
     */
    @RequestMapping(value = "/{mid}",method = RequestMethod.GET,produces = "text/html;charset=UTF-8")
    public Object detail(@RequestHeader(required = false) String token, @PathVariable long mid,
                         @RequestParam(defaultValue = "-1") int categoryId,
                         @RequestHeader(defaultValue = "-1") int category) throws BizException {

        userSessionService.assertSession(token);


        final long userId = userSessionService.getUid(token);
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);

        UserMessage userMessage = null;
        try {
            userMessage = userMessageService.findById(userId, mid);
        } catch (BizException e) {
            return e.getErrorResult().getMessage();
        }

        if (userMessage == null) {//消息未找到
            return CommonErrors.RESOURCE_NOT_FOUND.getMessage();
        }

        userMessageService.addMsgReadSet(userId, mid, catgory);

        //TODO此处可以优化
        String htmlContent = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "    <title>通知</title>\n" +
                "    <script type=\"text/javascript\">\n" +
                "    </script>\n" +
                "    <style type=\"text/css\">\n" +
                "        body {\n" +
                "            padding: 0px;\n" +
                "            margin: 0px;\n" +
                "            font-family: 微软雅黑, tahoma;\n" +
                "            font-size: 11pt;\n" +
                "            background-color: #f3f3f7;\n" +
                "            color: #636363;\n" +
                "        }\n" +
                "        .hddiv {\n" +
                "            padding-left: 18px;\n" +
                "            padding-top: 13px;\n" +
                "            padding-right: 16px;\n" +
                "            padding-bottom: 13px;\n" +
                "            margin-bottom: -14px;\n" +
                "            color: #636363;\n" +
                "            line-height: 27px;\n" +
                "            border-color: #636363;\n" +
                "            border-width: 1px;\n" +
                "            border-style: solid;\n" +
                "            border-radius: 15px;\n" +
                "            width: 76%;\n" +
                "            margin-top: 20px;\n" +
                "            margin-bottom: 20px;\n" +
                "            WORD-WRAP: break-word;\n" +
                "        }\n" +
                "        img {\n" +
                "            max-width:76%;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body bgcolor=\"#f3f3f7\">\n" +
                "<div align=\"center\" class=\"bdy\">\n" +
                "    <div style=\"text-indent: 1em; line-height: 27px;\" align=\"left\">\n" +
                userMessage.getTitle() +
                "    </div>\n" +
                "    <div style=\"text-indent: 1em;\" align=\"left\">\n" +
                DateFormatUtils.format(userMessage.getCreateTime(), "yyyy-MM-dd HH:mm:ss") +
                "    </div>\n" +
                "    <div class=\"hddiv\">\n" +
                userMessage.getContent() +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";

//        logger.info("消息内容content：{}",userMessage.getContent());
//        logger.info("消息内容htmlContent：{}",htmlContent);
        return htmlContent;
    }
}
