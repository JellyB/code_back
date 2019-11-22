package com.huatu.ztk.paper.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.PeriodTestRedisKey;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;

/**
 * 阶段测试自动交卷
 * 
 * @author zhangchong
 *
 */
@Component
public class PeriodTestAutoSubmitTask {
	private static final Logger logger = LoggerFactory.getLogger(PeriodTestAutoSubmitTask.class);

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private AnswerCardDao answerCardDao;

	@Autowired
	private PaperAnswerCardService paperAnswerCardService;
	
	@Autowired
	private UserDubboService userDubboService;

	@PostConstruct
	public void init() {
		// 添加停止任务线程
		Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
	}

	@Scheduled(fixedRate = 10 * 1000 * 10)
	public void submitPeriodTestAnswer() throws BizException {
		if (!getLock()) {
			return;
		}

		logger.info("auto submit PeriodTest answer task start.server={}", getServerIp());
		// 获取未交卷答题卡zset
		final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		long currentTimeStamp = System.currentTimeMillis();
		String periodTestAnswerCardUnfinshKey = PeriodTestRedisKey.getPeriodTestAnswerCardUnfinshKey();
		Set<String> zSetValue = zSetOperations.rangeByScore(PeriodTestRedisKey.getPeriodTestAnswerCardUnfinshKey(), 0,
				currentTimeStamp);
		if (!CollectionUtils.isEmpty(zSetValue)) {
			zSetValue.forEach(practiceIdStr -> {
				List<String> practiceIdStrList = Splitter.on("_").omitEmptyStrings().trimResults()
						.splitToList(practiceIdStr);
				if (practiceIdStrList.size() == 2) {
					// 自动交卷
					long practiceId = Long.parseLong(practiceIdStrList.get(0));
					StandardCard answerCard = (StandardCard) answerCardDao.findById(practiceId);
					if (answerCard != null) {
						long userId = answerCard.getUserId();
						if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
							try {
								UserDto user = userDubboService.findById(userId);
								if (user != null) {
									paperAnswerCardService.submitPeriodTestAnswerCard(practiceId, userId,
											new ArrayList<>(), AreaConstants.QUAN_GUO_ID,
											Long.parseLong(practiceIdStrList.get(1)), user.getName());
								}

							} catch (BizException e) {
								logger.error("auto submit PeriodTest error", e);
							}
							
						}
						
					}
				}
				// 清除set中已经处理的答题卡id
				zSetOperations.remove(periodTestAnswerCardUnfinshKey, practiceIdStr);

			});
		}

		unlock();
		logger.info("auto submit PeriodTest answer task end.server={}", getServerIp());
	}

	private static String getServerIp() {
		return System.getProperty("server_ip");
	}

	/**
	 * 释放定时任务锁
	 */
	private void unlock() {
		String lockKey = PeriodTestRedisKey.getPeriodTestAutoSubmitLockKey();
		String currentServer = (String) redisTemplate.opsForValue().get(lockKey);

		logger.info("auto submit periodTest current server={}", currentServer);
		if (getServerIp().equals(currentServer)) {
			redisTemplate.delete(lockKey);

			logger.info("release periodTest lock,server={},timestamp={}", currentServer, System.currentTimeMillis());
		}
	}

	/**
	 *
	 * @return 是否获得锁
	 */
	private boolean getLock() {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

		String lockKey = PeriodTestRedisKey.getPeriodTestAutoSubmitLockKey();

		String value = opsForValue.get(lockKey);

		logger.info("periodTest get lock timestamp={}", System.currentTimeMillis());
		if (StringUtils.isBlank(value)) { // 值为空
			boolean booleanValue = opsForValue.setIfAbsent(lockKey, getServerIp()).booleanValue();
			return booleanValue;
		} else if (StringUtils.isNoneBlank(value) && !value.equals(getServerIp())) {
			// 被其它服务器锁定
			logger.info("periodTest auto submit match lock server={},return", value);
			return false;
		} else { // 被自己锁定
			return true;
		}
	}
}
