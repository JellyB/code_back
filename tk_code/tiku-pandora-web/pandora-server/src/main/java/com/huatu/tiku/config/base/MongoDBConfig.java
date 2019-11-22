package com.huatu.tiku.config.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.consts.ApolloConfigConsts;
import org.springframework.context.annotation.Configuration;


/**
 * Created by huangqp on 2018\6\23 0023.
 */
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_MONGO)
@Configuration
public class MongoDBConfig {
}

