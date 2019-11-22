package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.common.utils.code.UUIDTools;
import com.huatu.common.utils.date.DateUtil;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.bean.reward.RewardResult;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.RewardActionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Calendar;
import java.util.Date;

/**
 * ONCE类型的任务直接在业务中拦截，此处不做处理
 * @author hanchao
 * @date 2017/10/13 16:07
 */
@Slf4j
public abstract class AbstractRewardActionEventHandler implements RewardActionEventHandler {
    //直接强制使用该key，屏蔽各应用的序列化前缀,防止跨应用任务的情况
    public static final String REWARD_BIZ_KEY = "AbstractRewardActionEventHandler.actionHit$%s$%s";
    @Autowired
    private RewardActionService rewardActionService;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();


    @Override
    public RewardResult handle(RewardActionEvent actionEvent) {
        if(actionEvent.getAction() == null){
            log.error("action cant be null,{}...",actionEvent);
        }
        if(filter(actionEvent)){
            RewardMessage rewardMessage = RewardMessage.builder()
                    .action(actionEvent.getAction().name())
                    .bizId(StringUtils.isBlank(actionEvent.getBizId()) ? UUIDTools.create():actionEvent.getBizId())
                    .gold(actionEvent.getGold())
                    .experience(actionEvent.getExperience())
                    .uid(actionEvent.getUid())
                    .uname(actionEvent.getUname())
                    .timestamp(actionEvent.getTimestamp())
                    .build();
            dealMessage(rewardMessage);
            RewardAction rewardAction = rewardActionService.get(actionEvent.getAction().name());
            return RewardResult.builder()
                    .completed(true)
                    .experience(actionEvent.getExperience() == 0 ? rewardAction.getExperience() : actionEvent.getExperience())
                    .gold(actionEvent.getGold() == 0 ? rewardAction.getGold() : rewardAction.getGold())
                    .build();
        }
        return null;
    }

    protected String getActionKey(RewardAction.ActionType action){
        return action.name();
    }

    public boolean filter(RewardActionEvent actionEvent){
        RewardAction rewardAction = rewardActionService.get(actionEvent.getAction().name());
        if(rewardAction.getStrategy() != RewardAction.Strategy.NONE){
            String cacheKey = String.format(REWARD_BIZ_KEY,getActionKey(rewardAction.getAction()),actionEvent.getUid());
            byte [] key = stringRedisSerializer.serialize(cacheKey);
            long expireTime = getExpireTime(rewardAction);
            //0不做限制
            if(expireTime != 0){
                Long reqTime = (Long)(getRedisTemplate().execute((RedisCallback<Long>) (conn)-> {
                    Long incr = conn.incr(key);
                    conn.expireAt(key,expireTime/1000);//pexpireAt有Bug，最终调用了pexpire
                    return incr;
                }));
                return analyzeTimes(rewardAction,reqTime);
            }
        }
        return true;
    }

    public long getExpireTime(RewardAction rewardAction){
        switch (rewardAction.getStrategy()){
            case DAILY:
                return DateUtil.getEndOfCurrentDay();
            case WEEKELY:
                return DateUtil.getEnd(Calendar.DAY_OF_WEEK,new Date()).getTimeInMillis();
            case MONTHLY:
                return DateUtil.getEnd(Calendar.DAY_OF_MONTH,new Date()).getTimeInMillis();
            case YEARLY:
                return DateUtil.getEnd(Calendar.YEAR,new Date()).getTimeInMillis();
            default:
                break;
        }
        return 0L;
    }

    /**
     * 每日特训专用，可以在里面对5判断，然后返回true
     * @param rewardAction
     * @param time 次数
     * @return
     */
    public boolean analyzeTimes(RewardAction rewardAction,long time){
        if(time > rewardAction.getTimesLimit()){
            return false;
        }
        return true;
    }


    @Override
    public boolean canHandle(RewardAction.ActionType actionType) {
        RewardAction rewardAction = rewardActionService.get(actionType.name());
        return canHandle(rewardAction);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    //所有应用要提供统一的redis数据源保证一致
    protected abstract RedisTemplate getRedisTemplate();

    public abstract void dealMessage(RewardMessage rewardMessage);

    protected abstract boolean canHandle(RewardAction rewardAction);
}
