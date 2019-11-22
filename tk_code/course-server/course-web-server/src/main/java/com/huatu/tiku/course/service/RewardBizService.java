package com.huatu.tiku.course.service;

import com.google.common.collect.Maps;
import com.huatu.common.utils.date.DateUtil;
import com.huatu.tiku.course.bean.RewardProgressDTO;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.RewardActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/10/14 11:32
 */
@Service
public class RewardBizService {
    @Autowired
    private RewardActionService rewardActionService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取用户任务完成进度
     * @param uid
     * @return
     */
	public Map<String, RewardProgressDTO> getRewardView(int uid) {
		Map<String, RewardProgressDTO> result = Maps.newHashMap();
		Map<String, RewardAction> all = rewardActionService.all();
		all.forEach((k, v) -> {
			if (v.getStrategy() != RewardAction.Strategy.ONCE && v.getStrategy() != RewardAction.Strategy.NONE) {
				int size = 0;
				//随堂练特殊处理
				if (v.getAction() == RewardAction.ActionType.COURSE_PRACTICE_RIGHT) {
					String userDailyKey = CoursePracticeCacheKey.getCoinDailyKey(uid);
					size = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(userDailyKey)).orElse(0);
				} else {
					String cacheKey = CourseCacheKey.rewardRecord(v.getAction().name(), uid);
					size = Math.toIntExact(Optional.ofNullable(redisTemplate.opsForSet().size(cacheKey)).orElse(0L));
				}
				RewardProgressDTO progressDTO = RewardProgressDTO.builder().action(k).bizName(v.getAction().getBizName()).limit(v.getTimesLimit()).time(size).build();
				result.put(k, progressDTO);
			}
		});
		return result;
	}

    /**
     * 添加用户任务到进度缓存
     * @param rewardAction
     * @param uid
     * @param bizId
     */
    public void addRewardAction(RewardAction rewardAction,int uid,String bizId){
        String cacheKey = CourseCacheKey.rewardRecord(rewardAction.getAction().name(),uid);

        redisTemplate.opsForSet().add(cacheKey,bizId);

        Date expireTime = new Date(System.currentTimeMillis() + 6000);//非法的默认一分钟过期
        switch (rewardAction.getStrategy()){
            case NONE:
            case ONCE:
                break;
            case DAILY:
                expireTime = DateUtil.getEndDateOfCurrentDay();
                break;
            case WEEKELY:
                expireTime = DateUtil.getEnd(Calendar.DAY_OF_WEEK,new Date()).getTime();
                break;
            case MONTHLY:
                expireTime = DateUtil.getEnd(Calendar.DAY_OF_MONTH,new Date()).getTime();
                break;
            case YEARLY:
                expireTime = DateUtil.getEnd(Calendar.YEAR,new Date()).getTime();
                break;
            default:
                break;
        }
        redisTemplate.expire(cacheKey,expireTime.getTime()-System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    }
}
