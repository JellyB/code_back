package com.arj.monitor.conf.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate redisTemplate = new RedisTemplate();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);

    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

    return redisTemplate;
  }
}
