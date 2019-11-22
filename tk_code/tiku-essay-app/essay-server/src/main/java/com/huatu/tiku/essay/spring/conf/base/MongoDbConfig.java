package com.huatu.tiku.essay.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.consts.ApolloConfigConsts;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lijun on 2018/10/10
 */
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_MONGO)
@Configuration
public class MongoDbConfig {
}
