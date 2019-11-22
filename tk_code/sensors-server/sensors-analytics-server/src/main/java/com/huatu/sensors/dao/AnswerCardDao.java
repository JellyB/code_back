package com.huatu.sensors.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.AnswerCardType;

import lombok.extern.slf4j.Slf4j;

/**
 * 答题卡dao层
 * 
 * @author zhangchong
 *
 */

@Repository
@Slf4j
public class AnswerCardDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * 根据id查询答题卡
	 *
	 * @param practiceId
	 * @return
	 */
	public AnswerCard findById(long practiceId) {
		final AnswerCard answerCard = mongoTemplate.findById(practiceId, AnswerCard.class);
		return answerCard;
	}

	/**
	 * 查询指定科目下模考大赛参加次数
	 * 
	 * @param userId
	 * @param subject
	 * @return
	 */
	public long countMockByUidAndSubject(long userId) {
		Criteria criteria = Criteria.where("userId").is(userId).and("type")
				.is(AnswerCardType.MATCH);
		Query query = new Query(criteria);
		log.info("countMockByUidAndSubject qury ={}", query);
		long count = mongoTemplate.count(query, AnswerCard.class);

		return count;
	}

}
