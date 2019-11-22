package com.huatu.tiku.match.spring.conf.db.tk.mapper.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_DB;

/**
 * Created by lijun on 2018/10/10
 */
@Data
@EnableApolloConfig(NAMESPACE_TIKU_DB)
@ConditionalOnProperty(name = "spring.datasource.master.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
@ConfigurationProperties(prefix = "spring.datasource")
public class MasterDataSourceProperties extends DruidDataSourceProperties{
}
