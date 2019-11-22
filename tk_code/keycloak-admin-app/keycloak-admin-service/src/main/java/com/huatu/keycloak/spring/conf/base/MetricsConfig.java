package com.huatu.keycloak.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Configuration;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_HT_METRICS;

/**
 * @author hanchao
 * @date 2017/9/22 15:26
 */
@Configuration
@EnableApolloConfig(NAMESPACE_HT_METRICS)
public class MetricsConfig {

}
