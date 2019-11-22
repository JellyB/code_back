package com.huatu.tiku.position.base.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 使用Apollo中Redis配置
 *
 * @author Geek-S
 *
 */
@Configuration
@EnableApolloConfig("tiku.redis-cluster")
public class ApolloConfigRedis {

    @Bean
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory){
        return new StringRedisTemplate(jedisConnectionFactory);
    }
}
