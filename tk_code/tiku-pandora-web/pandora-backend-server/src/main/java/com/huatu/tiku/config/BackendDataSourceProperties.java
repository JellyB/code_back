package com.huatu.tiku.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * 此处使用 apollo 中 tiku-templates 配置信息
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.teacher.master")
public class BackendDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private Integer initialSize;
    private Integer minIdle;
    private Integer maxActive;
    private Integer maxWait;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean poolPreparedStatements;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String filters;
    private Properties connectionProperties;

}
