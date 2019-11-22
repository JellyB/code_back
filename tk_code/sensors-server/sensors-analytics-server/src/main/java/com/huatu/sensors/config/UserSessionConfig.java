package com.huatu.sensors.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.google.common.base.Splitter;
import com.huatu.sensors.constants.RedisConstants;

import redis.clients.jedis.JedisPoolConfig;

/**
 * usersession配置
 * 
 * @author zhangchong
 *
 */
@Configuration
@EnableApolloConfig(RedisConstants.NAMESPACE_TIKU_USER_SESSIONS)
public class UserSessionConfig {

	@Value("${user-sessions.redis.pool.max-active}")
	private int maxActive;

	@Value("${user-sessions.redis.pool.max-idle}")
	private int maxIdle;

	@Value("${user-sessions.redis.pool.max-wait}")
	private int maxWaitMillis;

	@Value("${user-sessions.redis.pool.test-on-borrow}")
	private boolean testOnBorrow;

	@Value("${user-sessions.redis.sentinel.nodes}")
	private String nodes;

	@Value("${user-sessions.redis.sentinel.master}")
	private String masterName;

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(sessionJedisSentinelPoolConfig(),
				sessionJedisPoolConfig());
		jedisConnectionFactory.setUsePool(true);
		return jedisConnectionFactory;
	}

	@Bean
	public JedisPoolConfig sessionJedisPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		return poolConfig;
	}

	public RedisSentinelConfiguration sessionJedisSentinelPoolConfig() {
		RedisSentinelConfiguration conf = new RedisSentinelConfiguration();
		conf.master(masterName);
		List<String> nodeList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(nodes);
		nodeList.forEach(node -> {
			conf.addSentinel(RedisNode.newRedisNode()
					.listeningAt(node.split(":")[0], Integer.parseInt(node.split(":")[1])).build());
		});
		return conf;

	}

	@Bean(name = "sessionRedisTemplate")
	public StringRedisTemplate redisTemplate() {
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		return redisTemplate;
	}
}
