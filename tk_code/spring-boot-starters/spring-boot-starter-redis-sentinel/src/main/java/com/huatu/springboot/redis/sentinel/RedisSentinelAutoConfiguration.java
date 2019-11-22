package com.huatu.springboot.redis.sentinel;

import com.google.common.collect.Sets;
import com.huatu.common.spring.serializer.StringRedisKeySerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author hanchao
 * @date 2017/9/4 14:42
 */

@Configuration
@Slf4j
@EnableConfigurationProperties(SentinelProperties.class)
public class RedisSentinelAutoConfiguration {
    @Value("${spring.application.name:unknown}")
    private String applicationName;


    @Autowired
    private SentinelProperties sentinelProperties;


    @Autowired
	private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer;

    @Autowired
    private StringRedisKeySerializer stringRedisKeySerializer;

   
    
    @Bean(value = "sentinelPool")
    public JedisPoolConfig jedisPoolConfig() {
        log.info("Jedis Sentinel Pool  config initialize start ...");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(sentinelProperties.getPool().getMaxIdle());
        config.setMinIdle(sentinelProperties.getPool().getMinIdle());
        config.setMaxWaitMillis(sentinelProperties.getPool().getMaxWait());
        config.setMaxTotal(sentinelProperties.getPool().getMaxActive());
        config.setTestOnBorrow(sentinelProperties.getPool().isTestOnBorrow());
        log.info("sentinel pool config initialize end ...");
        return config;
    }
    


    /**
     * sentinelConfiguration
     *
     * @return
     */
    @Bean(value = "sentinelConfiguration")
    public RedisSentinelConfiguration sentinelConfiguration() {

        Set<String> sentinelHostAndPorts;
        String[] nodeArray = sentinelProperties.getSentinel().getNodes().split(",");
        //判断是否为空
        if (nodeArray == null || nodeArray.length == 0) {
            log.error("RedisClusterConfiguration initialize error nodeArray is null");
            throw new RuntimeException("RedisClusterConfiguration initialize error nodeArray is null");
        }

        sentinelHostAndPorts = Arrays.stream(nodeArray).collect(Collectors.toSet());
		RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration(sentinelProperties.getSentinel().getMaster(), sentinelHostAndPorts);
        return sentinelConfiguration;
    }
    
    @Bean(value = "sentinelJedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig, RedisSentinelConfiguration sentinelConfig) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(sentinelConfig, jedisPoolConfig);
        return jedisConnectionFactory;
    }
   

    @Primary
    @Bean(value = "redisTemplate")
    public RedisTemplate sentinelRedisTemplate(){
        RedisTemplate sentinelRedisTemplate = new RedisTemplate();
        sentinelRedisTemplate.setConnectionFactory(jedisConnectionFactory(jedisPoolConfig(),sentinelConfiguration()));
        sentinelRedisTemplate.setKeySerializer(stringRedisKeySerializer);
        sentinelRedisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer);
        return sentinelRedisTemplate;
    }
}
