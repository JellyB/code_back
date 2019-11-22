package com.huatu.tiku.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * @author zhouwei
 * @Description:
 * @create 2018-04-16 上午10:11
 **/

@Data
@ConfigurationProperties(prefix = "spring.datasource.teacher.master")
public class TeacherDataSourceProperties {
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
