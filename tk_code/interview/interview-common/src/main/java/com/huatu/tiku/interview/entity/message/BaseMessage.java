package com.huatu.tiku.interview.entity.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:25
 * @Modefied By:
 * 消息基类（普通用户 -> 公众帐号）
 */
@NoArgsConstructor
@Data
public class BaseMessage {
    // 接收方帐号（收到的OpenID）
    private String ToUserName;
    // 开发者微信号
    private String FromUserName;
    // 消息创建时间 （整型）
    private long CreateTime;
    // 消息类型（text/music/news）
    private String MsgType;
    // 位0x0001被标志时，星标刚收到的消息
    private int FuncFlag;
    // 消息id，64位整型
    private long MsgId;

    public BaseMessage(String toUserName, String fromUserName, long createTime, int funcFlag, long msgId) {
        ToUserName = toUserName;
        FromUserName = fromUserName;
        CreateTime = createTime;
        FuncFlag = funcFlag;
        MsgId = msgId;
    }
    public BaseMessage(Map<String, String> requestMap){
        // 发送方帐号（open_id）
        String fromUserName = requestMap.get("FromUserName");
        // 公众帐号
        String toUserName = requestMap.get("ToUserName");
         this.setToUserName(fromUserName);
        this.setFromUserName(toUserName);
        this.setCreateTime(new Date().getTime());

        this.setFuncFlag(0);
    }

}