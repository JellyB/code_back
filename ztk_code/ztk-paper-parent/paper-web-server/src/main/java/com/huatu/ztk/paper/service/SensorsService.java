package com.huatu.ztk.paper.service;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.util.SensorsUtils;
import com.huatu.ztk.user.common.UserRedisSessionKeys;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

/**
 * 神策分析相关逻辑
 * 
 * @author zhangchong
 *
 */
@Service
public class SensorsService {

	private static final Logger logger = LoggerFactory.getLogger(SensorsService.class);

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	@Resource(name = "sessionRedisTemplate")
	private RedisTemplate<String, String> sessionRedisTemplate;

	@Autowired
	private MatchDao matchDao;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	/**
	 * 模考大赛开始答题队列名称
	 */
	public static final String CREATE_MATCH_ANSWER_CARD_ANALYTICS_QUEUE = "create_match_answer_card_analytics_queue";

	/**
	 * 模考大赛结束答题队列名称
	 */
	public static final String SUBMIT_MATCH_ANSWER_CARD_ANALYTICS_QUEUE = "submit_match_answer_card_analytics_queue";

	/**
	 * 模考大赛确认报名神策埋点
	 * 
	 * @param paperId
	 * @param userId
	 * @param positionId
	 * @param subject
	 * @param terminal
	 */
	@Async
	public void mockEnroll(String token, Match match, Long userId, int positionId, int subject, int terminal) {
		try {
			logger.info("mockEnroll match---------" + match.toString());
			Map<String, Object> properties = Maps.newHashMap();
			String ucId = getSessionValue(token, UserRedisSessionKeys.ucId);
			properties.put("match_id", String.valueOf(match.getPaperId()));
			properties.put("match_title", String.valueOf(match.getName()));
			// properties.put("exam_type", String.valueOf(match.getTag() == 1 ? "国考" :
			// "省考"));
			properties.put("platform", SensorsUtils.getPlatform(terminal));
			properties.put("match_subject", "行测");
			logger.info("mockEnroll properties---------" + properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_SURESIGNUP.getCode(), properties);
			sensorsAnalytics.flush();
		} catch (InvalidArgumentException e) {
			logger.error("sa track error:" + e);
		}
	}

	/**
	 * 获取session中key的值
	 * 
	 * @param token
	 * @param key
	 * @return
	 */
	private String getSessionValue(String token, String key) {
		String value = null;
		if (StringUtils.isBlank(token)) {
			return null;
		}
		final HashOperations<String, String, String> hashOperations = sessionRedisTemplate.opsForHash();
		value = hashOperations.get(token, key);
		return value;
	}

	/**
	 * 模考大赛开始答题埋点
	 * 
	 * @param token
	 * @param id
	 * @param subject
	 * @param terminal
	 */
	@Async
	public void createMatchAnswerCardAnalytics(String token, int id, int subject, int terminal, StandardCard practice) {

		try {
			logger.info("createMatchAnswerCardAnalytics -----------");
			Map<String, Object> properties = Maps.newHashMap();
			String ucId = getSessionValue(token, UserRedisSessionKeys.ucId);
			Match match = matchDao.findById(id);
			// is_first_answer
			// match_class 公务员，教师
			properties.put("match_id", String.valueOf(match.getPaperId()));
			properties.put("match_title", String.valueOf(match.getName()));
			// properties.put("exam_type", String.valueOf(match.getTag() == 1 ? "国考" :
			// "省考"));
			properties.put("match_subject", "行测");
			properties.put("platform", SensorsUtils.getPlatform(terminal));
			logger.info("createMatchAnswerCardAnalytics properties---------" + properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), properties);
			sensorsAnalytics.flush();
		} catch (InvalidArgumentException e) {
			logger.error("sa track error:" + e);
		}
	}

	/**
	 * 模考大赛开始答题埋点v2
	 * 
	 * @param token
	 * @param id
	 * @param subject
	 * @param terminal
	 * @param practice
	 */
	@Async
	public void createMatchAnswerCardAnalyticsV2(String token, int id, int subject, int terminal,
			StandardCard practice) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("token", token);
		properties.put("id", id);
		properties.put("subject", subject);
		properties.put("practice", practice);
		rabbitTemplate.convertAndSend("", CREATE_MATCH_ANSWER_CARD_ANALYTICS_QUEUE, properties);
		logger.info("神策埋点模考开始答题接口用时={}", String.valueOf(stopwatch.stop()));
	}

	/**
	 * 模考大赛交卷埋点
	 * 
	 * @param token
	 * @param practiceId
	 * @param userId
	 * @param answers
	 * @param area
	 * @param uname
	 * @param cardType
	 */
	@Async
	public void submitEstimatePaperAnalytics(String token, long practiceId, long userId, int area, String uname,
			int cardType, int paperId, int expendTime, int terminal) {
		try {
			logger.info("submitEstimatePaperAnalytics start-----------");
			Map<String, Object> properties = Maps.newHashMap();
			String ucId = getSessionValue(token, UserRedisSessionKeys.ucId);
			Match match = matchDao.findById(paperId);
			// match_subject
			// match_class
			properties.put("match_answer_duration", String.valueOf(expendTime));
			properties.put("match_id", String.valueOf(match.getPaperId()));
			properties.put("match_title", String.valueOf(match.getName()));
			// properties.put("exam_type", String.valueOf(match.getTag() == 1 ? "国考" :
			// "省考"));
			properties.put("match_subject", "行测");
			properties.put("platform", SensorsUtils.getPlatform(terminal));
			logger.info("submitEstimatePaperAnalytics properties---------" + properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_ENDANSWER.getCode(), properties);
			sensorsAnalytics.flush();
		} catch (InvalidArgumentException e) {
			logger.error("sa track error:" + e);
		}
	}

	/**
	 * 模考大赛交卷埋点v2
	 * 
	 * @param token
	 * @param id
	 * @param subject
	 * @param terminal
	 * @param practice
	 */
	@Async
	public void submitEstimatePaperAnalyticsV2(String token, long practiceId, long userId, int area, String uname,
			int cardType, int paperId, int expendTime, int terminal) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Map<String, Object> properties = Maps.newHashMap();
		properties.put("token", token);
		properties.put("practiceId", practiceId);
		properties.put("userId", userId);
		properties.put("area", area);
		properties.put("uname", uname);
		properties.put("cardType", cardType);
		properties.put("paperId", paperId);
		properties.put("expendTime", expendTime);
		properties.put("terminal", terminal);
		rabbitTemplate.convertAndSend("", SUBMIT_MATCH_ANSWER_CARD_ANALYTICS_QUEUE, properties);
		logger.info("神策埋点模考结束答题接口用时={}", String.valueOf(stopwatch.stop()));
	}

}
