package com.huatu.tiku.interview.userHandler.message;

import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/12 9:52
 * @Modefied By:
 */
public interface MessageHandler {
    String TextMessageHandler(Map<String, String> requestMap);
    String ImageMessageHandler(Map<String, String> requestMap);
}
