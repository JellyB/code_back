package com.huatu.tiku.match.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.consts.ApolloConfigConsts;
import org.springframework.context.annotation.Configuration;

/**
 * 服务端点监控
 * @author hanchao
 * @date 2017/9/22 15:26
 */
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_HT_METRICS)
@Configuration
public class MetricsConfig {

}
