package com.huatu.sensors.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.huatu.sensors.utils.Dateutils;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MatchDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * 查询大赛信息
	 * 
	 * @param paperId
	 * @return
	 */
	public Match findById(int paperId) {
		return mongoTemplate.findById(paperId, Match.class);
	}

	/**
	 * 查询用户报名次数
	 * 
	 * @param userId
	 * @return
	 */
	public long countMatchUserMetaByUid(long userId) {
		Criteria criteria = Criteria.where("userId").is(userId);
		Query query = new Query(criteria);
		log.info("countMatchUserMetaByUid qury ={}", query);
		long count = mongoTemplate.count(query, MatchUserMeta.class);

		return count;
	}

	public List<Match> findLastDayComplateMatch() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day - 1);
		long startTime = Dateutils.getBeginOfDay(calendar.getTime());
		long endTime = Dateutils.getEndOfDay(calendar.getTime());
		Criteria criteria = Criteria.where("endTime").lte(endTime).and("startTime").gte(startTime).and("status").is(2);// 审核通过
		Query query = new Query(criteria);
		query.with(new Sort(Sort.Direction.ASC, "startTime"));
		List<Match> list = mongoTemplate.find(query, Match.class);
		return list;
	}
	
	
	public List<Match> findComplateMatch() {
		int minmatchId = 4001822;
		int maxMatchId = 4001988;
		Criteria criteria = Criteria.where("status").is(2).andOperator(
                  Criteria.where("_id").gte(minmatchId),
                  Criteria.where("_id").lte(maxMatchId));

		Query query = new Query(criteria);
		query.with(new Sort(Sort.Direction.ASC, "startTime"));
		System.out.println(query);
		List<Match> list = mongoTemplate.find(query, Match.class);
		return list;
	}
	


}
