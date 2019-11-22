package com.huatu.tiku.schedule.base.config;

import org.springframework.context.annotation.Configuration;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

/**
 * 使用Apollo中Redis配置
 * 
 * @author Geek-S
 *
 */
@Configuration
@EnableApolloConfig("tiku.redis-cluster")
public class ApolloConfigRedis {
}
