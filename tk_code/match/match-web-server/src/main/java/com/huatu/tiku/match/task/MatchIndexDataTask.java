package com.huatu.tiku.match.task;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.huatu.common.exception.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.tiku.match.bo.CourseInfoBo;
import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.manager.MatchManager;
import com.huatu.tiku.match.util.IPUtil;
import com.huatu.tiku.match.ztk.api.CourseFeignClient;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Match;

import lombok.extern.slf4j.Slf4j;

/**
 * 模考首页列表定时刷新redis数据
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class MatchIndexDataTask {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private MatchManager matchManager;

	@Autowired
	private CourseFeignClient courseFeignClient;

	public static String SUBJECTSTR = "1,2,24,200100054,200100055,200100056,200100057,100100262,200100049,200100051,200100050,200100052,200100063,100100175";

	@PostConstruct
	public void init() {
		// 添加停止任务线程
		Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
	}

	@Scheduled(fixedRate = 1000 * 60 * 5)
	public void updateMathInfo2Redis() {
		try {
			if (!getLock()) {
				return;
			}
			ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
			Arrays.asList(SUBJECTSTR.split(",")).forEach(subjectStr -> {
				Integer subject = Ints.tryParse(subjectStr);
				try {
					List<Match> matches = matchManager.findMatches(subject);
					if (CollectionUtils.isNotEmpty(matches)) {
						String matchListKey = MatchInfoRedisKeys.getMatchListKey(subject);
						valueOperations.set(matchListKey, JsonUtil.toJson(matches));
						redisTemplate.expire(matchListKey, 1, TimeUnit.HOURS);
						// 更新解析课信息
						 matches.forEach(match -> updateCourseInfo(match.getCourseId()));

					}

				} catch (BizException e) {
					e.printStackTrace();
				} finally {
					unlock();
				}
			});
			//
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			unlock();
		}

	}

	private void updateCourseInfo(int classId) {
		Map<String, Object> params = Maps.newHashMap();
		LinkedHashMap linkedHashMap = Maps.newLinkedHashMap();
		CourseInfoBo courseInfoBo = new CourseInfoBo();
		courseInfoBo.setClassId(classId);
		String key = MatchInfoRedisKeys.getCourseAnalysisInfo(classId);
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		params.put("classIds", classId);
		FeignResponse feignResponse = courseFeignClient.analysis(params);
		if (null != feignResponse.getData()) {
			linkedHashMap = (LinkedHashMap) feignResponse.getData();
			if (linkedHashMap.containsKey(String.valueOf(classId))) {
				LinkedHashMap content = (LinkedHashMap) linkedHashMap.get(String.valueOf(classId));
				if (content.containsKey(CourseFeignClient.LIVE_DATE) && content.containsKey(CourseFeignClient.PRICE)) {
					courseInfoBo
							.setLiveDate(NumberUtils.toLong(String.valueOf(content.get(CourseFeignClient.LIVE_DATE))));
					courseInfoBo.setPrice(NumberUtils.toInt(String.valueOf(content.get(CourseFeignClient.PRICE))));
					if (content.get(CourseFeignClient.COURSE_TITLE) == null) {
						valueOperations.set(key, JSONObject.toJSONString(courseInfoBo), 1, TimeUnit.MINUTES);
					} else {
						valueOperations.set(key, JSONObject.toJSONString(courseInfoBo), 1, TimeUnit.HOURS);
					}
				}
			} else {
				log.info("obtain course info from php client error, classId:{}", classId);
			}
		}
	}

	/**
	 * 释放定时任务锁
	 */
	private void unlock() {
		String lockKey = MatchInfoRedisKeys.MATCH_INDEX_DATA_UPDATE_KEY;
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		String currentServer = valueOperations.get(lockKey);

		log.info("current server={}", currentServer);
		if (IPUtil.getLocalIP().equals(currentServer)) {
			redisTemplate.delete(lockKey);
			log.info("release lock,server={},timestamp={}", currentServer, System.currentTimeMillis());
		}
	}

	/**
	 * @return 是否获得锁
	 */
	private boolean getLock() {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

		String lockKey = MatchInfoRedisKeys.MATCH_INDEX_DATA_UPDATE_KEY;
		String value = opsForValue.get(lockKey);
		log.info("get lock timestamp={}", System.currentTimeMillis());
		if (StringUtils.isBlank(value)) { // 值为空
			boolean booleanValue = opsForValue.setIfAbsent(lockKey, IPUtil.getLocalIP()).booleanValue();
			redisTemplate.expire(lockKey, 3, TimeUnit.MINUTES);
			return booleanValue;
		} else if (StringUtils.isNoneBlank(value) && !value.equals(IPUtil.getLocalIP())) {
			// 被其它服务器锁定
			log.info("MatchIndexDataTask lock server={},return", value);
			return false;
		} else { // 被自己锁定
			return true;
		}
	}

}
