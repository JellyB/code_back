package com.huatu.tiku.interview.entity.message;

import lombok.Data;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:49
 * @Modefied By:
 * 链接消息
 */
@Data
public class LinkMessage extends BaseMessage {
    // 消息标题
    private String Title;
    // 消息描述
    private String Description;
    // 消息链接
    private String Url;
}
