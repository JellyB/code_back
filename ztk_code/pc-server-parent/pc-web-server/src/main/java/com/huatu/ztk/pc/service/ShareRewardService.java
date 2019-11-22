package com.huatu.ztk.pc.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.dao.ShareDao;
import com.huatu.ztk.user.bean.RewardMessage;
import com.huatu.ztk.user.common.UserRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.commons.RewardConstants.*;

/**
 * Created by linkang on 2017/10/13 上午10:56
 */

@Service
public class ShareRewardService {

    private static final Logger logger = LoggerFactory.getLogger(ShareRewardService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ShareDao shareDao;


    /**
     * 每日分享次数限制
     */
    public static final int DAILY_SHARE_LIMIT = 2;

    /**
     * 记录分享用户name信息
     *
     * @param username
     * @param paperId
     */
    public void recordShareMatchUser(String username, int paperId, String key, int limit) {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        if (setOperations.size(key) < limit) {
            setOperations.add(key, username);
        }
    }

    /**
     * 分享加积分
     *
     * @param userId
     */
    public RewardMessage sendShareMsg(long userId, String uname, String shareId) throws BizException {
        Share share = shareDao.findById(shareId);

        if (share == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        String key = UserRedisKeys.getDayRewardKey(userId);

        RewardMessage msg = RewardMessage.builder()
                .action(ACTION_SHARE)
                .bizId(shareId)
                .uid(userId)
                .uname(uname)
                .timestamp(System.currentTimeMillis())
                .gold(10)
                .experience(10)
                .build();


        String hashKey = ACTION_SHARE;
        HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();

        if (String.valueOf(DAILY_SHARE_LIMIT).equals(opsForHash.get(key, hashKey))) {
            return new RewardMessage();
        }

        Boolean exists = redisTemplate.hasKey(key);
        opsForHash.increment(key, hashKey, 1);

        if (!exists) {
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }

        rabbitTemplate.convertAndSend("", MQ_NAME, msg);

        logger.info("send msg={},redis key={},hashkey={}", JsonUtil.toJson(msg), key, hashKey);

        return msg;
    }
}
