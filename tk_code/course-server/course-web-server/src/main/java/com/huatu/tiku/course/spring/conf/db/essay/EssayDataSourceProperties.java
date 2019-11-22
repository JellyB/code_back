package com.huatu.tiku.course.spring.conf.db.essay;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_DB_VHUATU_SLAVE;

/**
 * Created by lijun on 2018/6/19
 */
@EnableApolloConfig(NAMESPACE_DB_VHUATU_SLAVE)
@ConfigurationProperties(prefix = "spring.datasource.essay")
@Data
public class EssayDataSourceProperties {
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
