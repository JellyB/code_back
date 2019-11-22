package com.huatu.ztk.paper.service;

import static org.hamcrest.CoreMatchers.nullValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.dubbo.common.json.JSONObject;
import com.huatu.tiku.entity.common.User;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.PeriodTestConstant;
import com.huatu.ztk.paper.common.PeriodTestRedisKey;
import com.huatu.ztk.paper.common.RabbitMqConstants;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.service.v4.PeriodTestService;
import com.huatu.ztk.paper.vo.CourseStatisticsInfo;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author zhangchong
 *
 */
public class PeriodPaperServiceTest extends BaseTest {

	@Autowired
	private PeriodTestService periodTestService;

	@Resource
	private RedisTemplate redisTemplate;

	@Autowired
	private AnswerCardDao answerCardDao;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UserDubboService userDubboService;
	final int uid = 12252065;

	@Test
	public void testGet() {
		// periodTestService.incrementPeriodTestAnswerCardCount(3528236);
		// System.out.println("交卷人数:" + periodTestService.getSubmitCount(3528236));

		HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
		opsForHash.increment(PeriodTestRedisKey.getPeriodTestPaper(3528366, 11), "60", 1);
		Map<Object, Object> retMap = opsForHash.entries(PeriodTestRedisKey.getPeriodTestPaper(3528366, 11));
		System.out.println(retMap.toString());
		for (Map.Entry<Object, Object> entry : retMap.entrySet()) {
			Double double1 = Double.parseDouble(entry.getKey().toString());
			System.out.print(double1.intValue());
			int value = Integer.parseInt(entry.getValue().toString());
			System.out.print("---");
			System.out.println(value);
		}

	}

	@Test
	public void testZset() {
		ZSetOperations zSetOperations = redisTemplate.opsForZSet();
		Long endTime = System.currentTimeMillis() - 100;
		String setValueString = 1111 + "_" + 2222;
		zSetOperations.add(PeriodTestRedisKey.getPeriodTestAnswerCardUnfinshKey(), setValueString, endTime);
		// 取值

	}

	@Test
	public void testUpte() {
		// answerCardDao.update(2001846791472939008L, Update.update("syllabusId", 111));
		// Set<Long> ids = new HashSet<Long>();
		// ids.add(2001846791472939008L);
		// ids.add(2001846791472939008L);
		Set<String> ids = new HashSet<String>();
		ids.add("2001846791472939008");
		ids.add("2001846791472939008");
		List<Long> idsLongs = ids.stream().map(id -> Long.parseLong(id)).collect(Collectors.toList());
		Criteria criteria = Criteria.where("_id").in(idsLongs);
		Query query = new Query(criteria);
		List<AnswerCard> list = mongoTemplate.find(query, AnswerCard.class);
		list.forEach(answerCard -> logger.info(answerCard.getName()));
	}

	@Test
	public void testRank() {
		final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
		String redisKey = "testrank1_";
		// 分数+时间戳 排名
		long updateTime = System.currentTimeMillis();
		double timeRank = 55 + 1 - updateTime / Math.pow(10, (int) Math.log10(updateTime) + 1);
		long updateTime2 = updateTime + 100;
		double timeRank2 = 55 + 1 - updateTime2 / Math.pow(10, (int) Math.log10(updateTime2) + 1);

		System.err.print("timeRank is: " + timeRank + " timeRank2 is: " + timeRank2);

		zSetOperations.add(redisKey, "111", timeRank);
		zSetOperations.add(redisKey, "222", timeRank2);
		zSetOperations.add(redisKey, "333", 56);

		Long rank111 = zSetOperations.reverseRank(redisKey, "111");
		Long rank222 = zSetOperations.reverseRank(redisKey, "222");
		Long rank333 = zSetOperations.reverseRank(redisKey, "333");
		System.err.println("111 排名:" + (rank111 + 1));
		System.err.println("222 排名:" + (rank222 + 1));
		System.err.println("333 排名:" + (rank333 + 1));
		// 获取前十名value值
		logger.info("------获取前十名答题卡id---------");
		Set reverseRangeByScore = zSetOperations.reverseRangeByScore(redisKey, 0, 100, 0, 10);
		reverseRangeByScore.forEach(str -> logger.info(str));
		logger.info("------获取前十名答题卡id和分数---------");
		Set<TypedTuple<String>> reverseRangeByScoreWithScores = zSetOperations.reverseRangeByScoreWithScores(redisKey,
				0, 100, 0, 10);
		reverseRangeByScoreWithScores.forEach(str -> logger.info(str.getValue() + "------>" + str.getScore()));

	}

	@Test
	public void testMq() {
		PeriodTestSubmitlPayload payload = PeriodTestSubmitlPayload.builder().isFinish(1).syllabusId(8362039L)
				.userName("app_ztk620567022").papeId(3528397).userId(233982082L).build();
		rabbitTemplate.convertAndSend("", RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO, JsonUtil.toJson(payload));
		rabbitTemplate.convertAndSend("", RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO, JsonUtil.toJson(payload));
		rabbitTemplate.convertAndSend("", RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO, JsonUtil.toJson(payload));
	}

	@Test
	public void testCourseData() {
		HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
		opsForHash.increment(PeriodTestRedisKey.getPeriodTestAccuracyNum(3528366, 11), PeriodTestConstant.LTFIFTY, 1);
		opsForHash.increment(PeriodTestRedisKey.getPeriodTestAccuracyNum(3528366, 11), PeriodTestConstant.LTEIGHTY, 2);
		opsForHash.increment(PeriodTestRedisKey.getPeriodTestAccuracyNum(3528366, 11), PeriodTestConstant.GTEIGHTY, 3);

		opsForHash.increment(PeriodTestRedisKey.getPeriodTestQuestionAccuracy(3528366, 11), "3528366", 60);
		opsForHash.increment(PeriodTestRedisKey.getPeriodTestQuestionAccuracy(3528366, 11), "3528366", 70);

		CourseStatisticsInfo courseStatisticsInfo = periodTestService.getCourseData(3528366, 11,11);
	}
	
	@Test
	public void getAnswerCards(){
		String enctype = "application/x-www-form-urlencoded";
		HttpHeaders httpHeaders = new HttpHeaders();
		MediaType mediaType = MediaType.parseMediaType(enctype);
		httpHeaders.setContentType(mediaType);
		httpHeaders.add("Accept", org.springframework.http.MediaType.APPLICATION_JSON.toString());
		List <AnswerCard> list=periodTestService.getAnswerCardByType(AnswerCardType.FORMATIVE_TEST_ESTIMATE);
		for(int i=0;i<list.size();i++){
			UserDto user=userDubboService.findById(list.get(i).getUserId());
			String userName=user.getName();
			Long syllabusId=list.get(i).getSyllabusId();
			if(syllabusId != null) {
				String url="userName="+userName+"&syllabusId="+syllabusId+"&isFinish="+1;
				HttpEntity<String> formEntity = new HttpEntity<String>(url, httpHeaders);
				String result = restTemplate.postForObject("http://testapi.huatu.com/lumenapi/v5/c/class/stage_test_study_record", formEntity, String.class);
				System.out.print(list.get(i).getId()+"----------"+result);
			}
		}

	}

}
