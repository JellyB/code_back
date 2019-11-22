package com.huatu.tiku.match.spring.conf.db.tk.mapper.config;

import lombok.Data;

import java.util.Properties;

/**
 * Created by lijun on 2018/6/27
 */
@Data
public class DruidDataSourceProperties {

    /**
     * @see {@link org.springframework.boot.autoconfigure.jdbc.DataSourceProperties#url}
     */
    private String url;
    /**
     * @see {@link org.springframework.boot.autoconfigure.jdbc.DataSourceProperties#username}
     */
    private String username;
    /**
     * @see {@link org.springframework.boot.autoconfigure.jdbc.DataSourceProperties#password}
     */
    private String password;

    /**
     * 是否只读库
     */
    private boolean readonly;
    /**
     * 权重，方便负载均衡使用
     */
    private int weight;

    private String driverClassName;
    private Integer initialSize;
    private Integer minIdle;
    private Integer maxActive;
    private Integer maxWait;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;

    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean poolPreparedStatements = true;
    private Integer psCacheSize;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String filters;
    private String validationQuery;
    private Properties connectionProperties;
}
