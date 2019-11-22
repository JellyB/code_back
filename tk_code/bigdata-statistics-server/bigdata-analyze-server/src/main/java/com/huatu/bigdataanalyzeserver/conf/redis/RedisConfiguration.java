package com.huatu.bigdataanalyzeserver.conf.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfiguration {

    @Bean
    public Jedis createJedis() {
        Jedis jedis = new Jedis("192.168.100.26", 6379);

        return jedis;
    }
}
