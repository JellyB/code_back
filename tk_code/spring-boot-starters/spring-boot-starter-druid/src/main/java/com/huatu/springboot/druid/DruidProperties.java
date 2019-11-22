package com.huatu.springboot.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/1/14 22:49
 */
@ConfigurationProperties(prefix = "spring.datasource.druid")
@Data
public class DruidProperties extends DruidDataSourceProperties {
    private MonitorProperties monitor = new MonitorProperties();
    private Map<String,DruidDataSourceProperties> datasources;

    @Data
    public static class MonitorProperties {
        private String druidStatView;
        private String druidWebStatFilter;
        private String allow;
        private String deny;
        private String loginUsername;
        private String loginPassword;
        private String exclusions;
        private String resetEnable;
    }

}
