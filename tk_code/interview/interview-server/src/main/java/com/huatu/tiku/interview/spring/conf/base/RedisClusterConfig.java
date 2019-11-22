package com.huatu.tiku.interview.spring.conf.base;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.spring.serializer.StringRedisKeySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_REDIS;

/**
 * @author hanchao
 * @date 2017/9/4 14:42
 */
@EnableApolloConfig(NAMESPACE_TIKU_REDIS)
@Configuration
public class RedisClusterConfig {
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Bean
    public StringRedisKeySerializer stringRedisKeySerializer(){
        return new StringRedisKeySerializer(applicationName);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory){
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

    @Bean
    public RedisTemplate redisTemplate(StringRedisKeySerializer stringRedisKeySerializer,GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer,JedisConnectionFactory jedisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisKeySerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer);
        return redisTemplate;
    }


}
