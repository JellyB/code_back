package com.huatu.tiku.springboot.users.core;

import com.google.common.collect.Sets;
import com.huatu.tiku.springboot.users.support.UserSessionRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

/**
 * 减少对auto redis的侵入，所以不使用spring data提供的tempalte
 * @author hanchao
 * @date 2017/8/23 10:54
 */
@Configuration
@EnableConfigurationProperties(UserSessionRedisProperties.class)
public class SessionRedisConfiguration {

    private final UserSessionRedisProperties properties;

    public SessionRedisConfiguration(UserSessionRedisProperties properties){
        this.properties = properties;
    }


    /**
     * sentinel连接池
     * @return
     */
    @Bean
    public JedisSentinelPool jediSentinelPool(){
        JedisPoolConfig poolConfig = this.properties.getPool() != null ? jedisPoolConfig() : new JedisPoolConfig();
        UserSessionRedisProperties.Sentinel sentinelConfig = properties.getSentinel();
        Set<String> sentinelNodes = Sets.newHashSet(sentinelConfig.getNodes().split(","));
        JedisSentinelPool sentinelPool = new JedisSentinelPool(sentinelConfig.getMaster(),sentinelNodes,poolConfig,this.properties.getPassword());
        return sentinelPool;
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        UserSessionRedisProperties.Pool props = this.properties.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        config.setTestOnBorrow(props.isTestOnBorrow());
        return config;
    }
}
