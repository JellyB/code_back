package com.huatu.sensors.config.db.pandora;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_DB_VHUATU_SLAVE;

/**
 * 申论数据源
 * @author zhangchong
 *
 */
@EnableApolloConfig("tiku.pandora.db")
@ConfigurationProperties(prefix = "spring.datasource.pandora")
@Data
public class PandoraDataSourceProperties {
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
