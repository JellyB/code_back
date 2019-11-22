package interview.conf.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * Author: xuhuiqiang
 * Time: 2018-09-27  17:58 .
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class InterviewDataSourceProperties {
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
