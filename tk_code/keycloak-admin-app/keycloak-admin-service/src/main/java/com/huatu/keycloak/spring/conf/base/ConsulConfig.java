package com.huatu.keycloak.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanchao
 * @date 2017/10/19 13:27
 */
@Configuration
@EnableApolloConfig("tiku.consul")
public class ConsulConfig {

}
