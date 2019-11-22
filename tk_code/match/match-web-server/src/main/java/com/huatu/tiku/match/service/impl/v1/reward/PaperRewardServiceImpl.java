package com.huatu.tiku.match.service.impl.v1.reward;

import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.match.common.UserRedisKeys;
import com.huatu.tiku.match.service.v1.reward.PaperRewardService;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.commons.RewardConstants.*;

/**
 * Created by linkang on 2017/10/12 下午7:33
 */
@Slf4j
@Service
public class PaperRewardServiceImpl implements PaperRewardService{

    /**
     * 每日/每周可以加积分的次数
     */
    public static final int SEND_LIMIT = 1;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplateWithoutServerName;


    /**
     * 报名加积分
     *
     * @param userId
     * @param uname
     * @param paperId
     */
    @Override
    public void sendEnrollMsg(long userId, String uname, int paperId) {
        String key = UserRedisKeys.getWeekRewardKey(userId);

        RewardMessage msg = RewardMessage.builder()
                .action(ACTION_MATCH_ENROLL)
                .bizId(userId + "_" + paperId)
                .uid(new Long(userId).intValue())
                .uname(uname)
                .build();

        sendMsg(key, ACTION_MATCH_ENROLL, 7, TimeUnit.DAYS, msg, SEND_LIMIT);
    }


    /**
     * 模考大赛交卷加积分
     *
     * @param userId
     * @param uname
     * @param practiceId
     */
    @Override
    public void sendMatchSubmitMsg(long userId, String uname, long practiceId) {
        String key = UserRedisKeys.getWeekRewardKey(userId);

        RewardMessage msg = RewardMessage.builder()
                .action(ACTION_MATCH_ENTER)
                .bizId(practiceId + "")
                .uid(new Long(userId).intValue())
                .uname(uname)
                .build();
        sendMsg(key, ACTION_MATCH_ENTER, 7, TimeUnit.DAYS, msg, SEND_LIMIT);
    }


    /**
     * 发送到队列
     *
     * @param key
     * @param hashKey
     * @param timeout
     * @param unit
     * @param msg
     */
    private void sendMsg(String key, String hashKey, final long timeout, final TimeUnit unit,
                         RewardMessage msg, int limit) {
        //统一在此处设置时间戳
        msg.setTimestamp(System.currentTimeMillis());

        HashOperations<String, Object, Object> opsForHash = redisTemplateWithoutServerName.opsForHash();

        if (String.valueOf(limit).equals(opsForHash.get(key, hashKey))) {
            return;
        }

        boolean exists = redisTemplateWithoutServerName.hasKey(key);
        opsForHash.increment(key, hashKey, 1);

        if (!exists) {
            redisTemplateWithoutServerName.expire(key, timeout, unit);
        }

        rabbitTemplate.convertAndSend("", MQ_NAME, msg);

        log.info("send msg={},redis key={},hashkey={}", JsonUtil.toJson(msg), key, hashKey);
    }
}
