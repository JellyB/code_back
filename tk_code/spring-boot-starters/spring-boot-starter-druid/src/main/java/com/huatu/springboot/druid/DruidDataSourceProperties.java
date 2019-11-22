package com.huatu.springboot.druid;

import lombok.Data;

import java.util.Properties;

/**
 * @author hanchao
 * @date 2018/4/17 16:09
 */
@Data
public class DruidDataSourceProperties {
    //仅多数据源时有用，默认单数据源的加载适配spring自己本身的配置
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
