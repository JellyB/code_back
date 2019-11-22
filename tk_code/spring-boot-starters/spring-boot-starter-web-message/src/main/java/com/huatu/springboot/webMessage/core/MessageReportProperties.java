package com.huatu.springboot.webMessage.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 存放全局路过滤器的路径配置信息
 * Created by junli on 2018/4/8.
 */
@Data
@ConfigurationProperties(prefix = "tiku.web.message")
public class MessageReportProperties {

    private WebProducerProperties web = new WebProducerProperties();

    @Data
    public static class WebProducerProperties {
        //未使用
        @Deprecated
        private boolean async = false;
        //匹配路径地址
        private String[] matches = new String[]{"/**"};
        //不匹配地址
        private String[] excludes;
    }
}
