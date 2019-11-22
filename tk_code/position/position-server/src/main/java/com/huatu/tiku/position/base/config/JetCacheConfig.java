package com.huatu.tiku.position.base.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import com.huatu.tiku.position.biz.listener.JetCachePurgeListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 开启JetCache配置
 *
 * @author Geek-S
 */
@Configuration
@EnableApolloConfig("jetcache")
@EnableMethodCache(basePackages = "com.huatu.tiku.position.biz")
public class JetCacheConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, JetCachePurgeListener jetCachePurgeListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new MessageListenerAdapter(jetCachePurgeListener), new ChannelTopic(JetCacheConstant.PURGE_CACHE_CHANNEL));

        return container;
    }
}
