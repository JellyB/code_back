package com.huatu.tiku.interview.entity.message;

import lombok.Data;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 19:52
 * @Modefied By:
 * 音频消息
 */
@Data
public class VoiceMessage extends BaseMessage {
    // 媒体ID
    private String MediaId;
    // 语音格式
    private String Format;
}
