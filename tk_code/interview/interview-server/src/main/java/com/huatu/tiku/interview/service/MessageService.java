package com.huatu.tiku.interview.service;


/**
 * @Author ZhenYang
 * @Date Created in 2018/1/18 23:42
 * @Description
 */

public interface MessageService {
    /**
     * 推送模板消息
     * @param accessToken
     * @param data
     * @return
     */
    String sendTemplate(String accessToken, String data);
}
