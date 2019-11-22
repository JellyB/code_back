package com.huatu.tiku.course.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.common.test.BaseWebTest;
import com.huatu.common.utils.date.TimeMark;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.bean.practice.PracticeUserQuestionMetaInfoBo;
import com.huatu.tiku.course.common.CoinType;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.dao.manual.CourseLiveBackLogMapper;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.course.service.v1.practice.PracticeUserMetaService;
import com.huatu.tiku.course.service.v6.PeriodTestServiceV6;
import com.huatu.tiku.course.util.EncryptUtils;
import com.huatu.tiku.entity.CourseLiveBackLog;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 阶段测试
 */
@Slf4j
public class PeriodTest extends BaseWebTest {
	@Autowired
	private PeriodTestServiceV6 periodTestServiceV6;

	@Autowired
	private EncryptUtils encryptUtil;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CourseLiveBackLogMapper courseLiveBackLogMapper;

	@Autowired
	private CourseLiveBackLogService courseLiveBackLogService;

	@Autowired
	@Qualifier(value = "persistTemplate")
	private RedisTemplate persistTemplate;
	
	@Autowired
	private PracticeUserMetaService practiceUserMetaService;

	@Test
	public void testUpload() throws InterruptedException, ExecutionException, BizException {
		TimeMark timeMark = TimeMark.newInstance();
		PeriodTestSubmitlPayload payload = PeriodTestSubmitlPayload.builder().syllabusId(8361872L)
				.userName("app_ztk620567022").isFinish(1).build();
		periodTestServiceV6.uploadPeriodStatus2PHP(payload);
		log.info(">>>>>>>>> payload request complete,used {} mills,total cost {} mills...", timeMark.mills(),
				timeMark.totalMills());

	}

	/**
	 * 获取直播回调地址
	 */
	@Test
	public void testBJY() {
		JSONObject json = encryptUtil.getClassCallBackUrl();
		log.info("获取直播回调地址:{}", json.toJSONString());
	}

	/**
	 * 设置直播回调地址
	 */
	@Test
	public void testSetCallBackUrlBJY() {
		JSONObject json = encryptUtil.setClassCallBackUrl();
		log.info("获取直播回调地址:{}", json.toJSONString());
	}

	public void testMapReduce() {

		List<PracticeUserQuestionMetaInfoBo> question = new ArrayList<>();
		question.add(PracticeUserQuestionMetaInfoBo.builder().correct(1).build());
		question.add(PracticeUserQuestionMetaInfoBo.builder().correct(1).build());
		Integer totalScore = question.stream()
				.map(practiceUserQuestionMetaInfoBo -> practiceUserQuestionMetaInfoBo.getCorrect() == 1 ? 2 : 0)
				.reduce(0, (a, b) -> a + b);
		log.info("总积分为:{}", totalScore);
	}

	/**
	 * 随堂练赠送金币
	 */
	@Test
	public void testCoin() {
		RewardMessage msg = RewardMessage.builder().gold(100000).uid(233982082).action(CoinType.COURSE_PRACTICE_RIGHT)
				.experience(10000).bizId(System.currentTimeMillis() + "").uname("app_ztk620567022")
				.timestamp(System.currentTimeMillis()).build();
		rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);
	}

	@Test
	public void testMapper() {
		courseLiveBackLogMapper.insertSelective(
				CourseLiveBackLog.builder().liveBackCoursewareId(111L).liveCoursewareId(998l).roomId(888L).build());
		SimpleCourseLiveBackLog findByRoomIdAndLiveCoursewareId = courseLiveBackLogService
				.findByRoomIdAndLiveCourseWareIdV2(888L, 998L);
		log.info("findByRoomIdAndLiveCoursewareId :{}", findByRoomIdAndLiveCoursewareId);
	}

	@Test
	public void testredisHash() {
		final SetOperations<String, Integer> setOperations = persistTemplate.opsForSet();
		String key_1 = CoursePracticeCacheKey.roomIdUserMetaKey(111L,111L, 2);
		setOperations.add(key_1,222);
		Long count = setOperations.size(key_1);
		if (count == null || count == 0) {
			count = 1L;
		}
		String key = "zhangchong123123";
		HashOperations<String, String, Integer> metaOpsForHash = persistTemplate.opsForHash();
		Map<String, Integer> metaEntries = metaOpsForHash.entries(key);
		Integer oldRcount = metaEntries.get(CoursePracticeCacheKey.RCOUNT);
		Integer oldTotal = metaEntries.get(CoursePracticeCacheKey.TOTALTIME);
		Integer totalRcount = 1;
		Integer totalTime = 1;
		metaOpsForHash.put(key,CoursePracticeCacheKey.RCOUNT, (oldRcount == null ? totalRcount : totalRcount + oldRcount));
		metaOpsForHash.put(key,CoursePracticeCacheKey.TOTALTIME, (oldTotal == null ? totalTime : totalTime + oldTotal));
		
		
		
		Integer rCount = metaOpsForHash.get(key,CoursePracticeCacheKey.RCOUNT);
		Integer rtotalTime = metaOpsForHash.get(key,CoursePracticeCacheKey.TOTALTIME);
		Map<String, Integer> retMap = Maps.newHashMap();
		retMap.put("classAverageTime", rtotalTime / count.intValue());
		retMap.put("classAverageRcount", rCount / count.intValue());
		
		System.out.println("----------->"+retMap);
	}
	
	@Test
	public void testMq() {
		Map map = Maps.newHashMap();
		map.put("questionId", 111);
		List list = Lists.newArrayList();
		list.add(map);
		rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE, list);
	}

}
