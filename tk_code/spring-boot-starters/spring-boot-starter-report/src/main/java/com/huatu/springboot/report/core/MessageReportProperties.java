package com.huatu.springboot.report.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hanchao
 * @date 2018/1/12 10:30
 */
@Data
@ConfigurationProperties(prefix = "tiku.report")
public class MessageReportProperties {
    private WebProducterProperties web = new WebProducterProperties();

    @Data
    public static class WebProducterProperties{
        //未使用
        @Deprecated
        private boolean async = false;
        private String[] matches = new String[]{"/**"};
        private String[] excludes;
    }
}
