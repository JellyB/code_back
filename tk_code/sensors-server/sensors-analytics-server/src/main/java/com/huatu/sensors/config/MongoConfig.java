package com.huatu.sensors.config;

import org.springframework.context.annotation.Configuration;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.consts.ApolloConfigConsts;

/**
 * mongo配置
 * 
 * @author zhangchong
 *
 */
@Configuration
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_MONGO)
public class MongoConfig {

}
