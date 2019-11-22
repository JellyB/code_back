package com.huatu.sensors.func;

import static org.mockito.Matchers.longThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.huatu.sensors.AppTest;
import com.huatu.sensors.dao.MatchDao;
import com.huatu.ztk.paper.bean.Match;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoT extends AppTest {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private MatchDao matchDao;

	@Test
	public void find() {
//		mongoTemplate.findAll(Match.class).forEach(match -> {
//			log.info(match.toString());
//		});

		//Match match = mongoTemplate.findById(3526734, Match.class);
		 Match match = mongoTemplate.findOne(new
		 Query(Criteria.where("paperId").is(3526734)), Match.class);
		log.info("match is {}", match.toString());
	}
	
	@Test
	public void count() {
		long count = matchDao.countMatchUserMetaByUid(23398208212L);
		log.info("match count is {}",count);
	}
	
	

}
