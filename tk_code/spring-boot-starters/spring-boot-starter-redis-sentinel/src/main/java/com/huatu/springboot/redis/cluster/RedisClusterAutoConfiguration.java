package com.huatu.springboot.redis.cluster;

import com.google.common.collect.Lists;
import com.huatu.common.spring.serializer.StringRedisKeySerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;


/**
 * @author hanchao
 * @date 2017/9/4 14:42
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(ClusterProperties.class)
public class RedisClusterAutoConfiguration {
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Autowired
    private ClusterProperties clusterProperties;

    @Bean
    public StringRedisKeySerializer stringRedisKeySerializer(){
        return new StringRedisKeySerializer(applicationName);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(@Autowired @Qualifier("clusterJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory){
        return new StringRedisTemplate(jedisConnectionFactory);
    }

    /**
     * 使用官方的，防止踩坑
     * @return
     */
    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean(value = "clusterPool")
    public JedisPoolConfig clusterPool() {
        log.info("cluster Jedis Pool  config initialize start ...");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(clusterProperties.getPool().getMaxIdle());
        config.setMinIdle(clusterProperties.getPool().getMinIdle());
        config.setMaxWaitMillis(clusterProperties.getPool().getMaxWait());
        config.setMaxTotal(clusterProperties.getPool().getMaxActive());
        config.setTestOnBorrow(clusterProperties.getPool().isTestOnBorrow());
        log.info("cluster Jedis Pool config initialize end ...");
        return config;
    }

    /**
     * sentinelConfiguration
     *
     * @return
     */
    @Bean(value = "clusterConfiguration")
    public RedisClusterConfiguration clusterConfiguration() {
        List<RedisNode> redisNodes = Lists.newArrayList();
        String[] nodeArray = clusterProperties.getCluster().getNodes().split(",");
        //判断是否为空
        if (nodeArray == null || nodeArray.length == 0) {
            log.error("RedisClusterConfiguration initialize error nodeArray is null");
            throw new RuntimeException("RedisClusterConfiguration initialize error nodeArray is null");
        }
        //循环注入至Set中
        for (String node : nodeArray) {
            String host = node.split(":")[0];
            int port = Integer.valueOf(node.split(":")[1]);
            RedisNode redisNode = new RedisNode(host, port);
            log.info("Read node : {}。", node);
            redisNodes.add(redisNode);
        }

        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setClusterNodes(redisNodes);
        clusterConfiguration.setMaxRedirects(clusterProperties.getCluster().getMaxRedirects());
        return clusterConfiguration;
    }

    @Bean(value = "clusterJedisConnectionFactory")
    public JedisConnectionFactory clusterJedisConnectionFactory(JedisPoolConfig jedisPoolConfig, RedisClusterConfiguration clusterConfiguration) {

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfiguration, jedisPoolConfig);
        return jedisConnectionFactory;
    }

    @Bean(name = "persistTemplate")
    public RedisTemplate redisTemplate(StringRedisKeySerializer stringRedisKeySerializer,GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(clusterJedisConnectionFactory(clusterPool(), clusterConfiguration()));
        redisTemplate.setKeySerializer(stringRedisKeySerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer);
        return redisTemplate;
    }

}
