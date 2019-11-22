package com.huatu.tiku.search.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Configuration;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_DUBBO;

/**
 * @author hanchao
 * @date 2018/1/8 15:39
 */
@Configuration
@EnableApolloConfig(value = NAMESPACE_TIKU_DUBBO)
public class DubboConfig {
}
