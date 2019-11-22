package com.huatu.one.base.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class WeChatConfig {
    @Value(value = "${weChat.appId}")
    public  String  appId;

    @Value(value = "${weChat.secret}")
    public  String secret;

    @Value(value = "${weChat.grantType}")
    public  String grantType;

    @Value(value = "${weChat.requestUrl}")
    public  String requestUrl;
}
