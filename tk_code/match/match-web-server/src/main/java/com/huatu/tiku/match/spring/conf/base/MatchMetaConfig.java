package com.huatu.tiku.match.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.tiku.match.common.MatchConfig;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-17 下午3:48
 **/
@EnableApolloConfig(value = MatchConfig.MATCH_PREFIX)
@Configuration
public class MatchMetaConfig {
}
