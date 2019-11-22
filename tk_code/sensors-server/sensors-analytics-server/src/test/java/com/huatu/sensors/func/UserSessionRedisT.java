package com.huatu.sensors.func;

import static org.mockito.Matchers.longThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import com.huatu.sensors.AppTest;
import com.huatu.sensors.dao.MatchDao;
import com.huatu.ztk.paper.bean.Match;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSessionRedisT extends AppTest {

	@Autowired
	private RedisTemplate sessionRedisTemplate;
	
	

	
	@Test
	public void test() {
		sessionRedisTemplate.opsForSet().add("zc0910", "100");
		Boolean member = sessionRedisTemplate.opsForSet().isMember("zc0910", "100");
		sessionRedisTemplate.delete("zc0910");
		log.info("redis ret is {}",member);
	}
	
	

}
