package com.huatu.tiku.interview.entity.message;

import com.huatu.tiku.interview.entity.Article;
import com.huatu.tiku.interview.util.MessageUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:26
 * @Modefied By:
 * 文本消息
 */
@Data
public class NewsMessage extends BaseMessage {
    // 图文消息个数，限制为10条以内
    private int ArticleCount;
    // 多条图文消息信息，默认第一个item为大图
    private List<Article> Articles;

    public NewsMessage(Map<String, String> requestMap) {
        super(requestMap);
        this.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
    }

    public NewsMessage(String toUserName, String fromUserName, long createTime, int funcFlag, long msgId) {
        super(toUserName, fromUserName, createTime, funcFlag, msgId);
        this.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
    }
}