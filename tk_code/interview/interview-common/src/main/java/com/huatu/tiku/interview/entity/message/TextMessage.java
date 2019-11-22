package com.huatu.tiku.interview.entity.message;

import com.huatu.tiku.interview.util.MessageUtil;
import lombok.Data;

import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:25
 * @Modefied By:
 * 文本消息
 */
@Data
public class TextMessage extends BaseMessage {
    // 消息内容
    private String Content = "";

    public TextMessage(String content,Map<String, String> requestMap) {
        super(requestMap);
        this.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
        Content = content;
    }

    public TextMessage(Map<String, String> requestMap) {
        super(requestMap);
        this.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
    }
}
