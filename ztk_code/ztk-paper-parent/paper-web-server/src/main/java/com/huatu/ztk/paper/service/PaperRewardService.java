package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.DayTrain;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.user.bean.RewardMessage;
import com.huatu.ztk.user.common.UserRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class PaperRewardService {

    private static final Logger logger = LoggerFactory.getLogger(PaperRewardService.class);

    /**
     * 每日特训完成次数
     */
    public static final int DAY_TRAIN_FINISH_COUNT = 5;

    /**
     * 每日/每周可以加积分的次数
     */
    public static final int SEND_LIMIT = 1;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DayTrainService dayTrainService;

    /**
     * 提交答题卡加积分
     *
     * @param userId
     * @param uname
     */
    public void sendSubmitPracticeMsg(long userId, String uname, AnswerCard answerCard) throws BizException {
      //  long stime = System.currentTimeMillis();

        final int cardType = answerCard.getType();
        final long practiceId = answerCard.getId();
        final int subject = answerCard.getSubject();

        switch (cardType) {
            case AnswerCardType.SMART_PAPER:
                sendDailySubmitMsg(userId, uname, practiceId, ACTION_TRAIN_INTELLIGENCEL);
                break;

            case AnswerCardType.CUSTOMIZE_PAPER:
                sendDailySubmitMsg(userId, uname, practiceId, ACTION_TRAIN_SPECIAL);
                break;

            case AnswerCardType.WRONG_PAPER:
                sendDailySubmitMsg(userId, uname, practiceId, ACTION_TRAIN_MISTAKE);
                break;

            case AnswerCardType.MATCH:
                sendMatchSubmitMsg(userId, uname, practiceId);
                break;

            case AnswerCardType.DAY_TRAIN:
                sendDailyTrainMsg(userId, uname, subject, practiceId);
                break;
        }
       // logger.info("userId={},practiceId={},type={},utime={}", userId, practiceId, cardType, System.currentTimeMillis() - stime);
    }


    /**
     * 日常练习
     *
     * @param userId
     * @param uname
     * @param practiceId
     */
    private void sendDailySubmitMsg(long userId, String uname, long practiceId, String action) {

        String key = UserRedisKeys.getDayRewardKey(userId);
        RewardMessage msg = RewardMessage.builder()
                .action(action)
                .bizId(practiceId + "")
                .uid(userId)
                .uname(uname)
                .build();
        sendMsg(key, action, 1, TimeUnit.DAYS, msg, SEND_LIMIT);
    }

    /**
     * 报名加积分
     *
     * @param userId
     * @param uname
     * @param paperId
     */
    public void sendEnrollMsg(long userId, String uname, int paperId) {
        String key = UserRedisKeys.getWeekRewardKey(userId);

        RewardMessage msg = RewardMessage.builder()
                .action(ACTION_MATCH_ENROLL)
                .bizId(userId + "_" + paperId)
                .uid(userId)
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
    private void sendMatchSubmitMsg(long userId, String uname, long practiceId) {
        String key = UserRedisKeys.getWeekRewardKey(userId);

        RewardMessage msg = RewardMessage.builder()
                .action(ACTION_MATCH_ENTER)
                .bizId(practiceId + "")
                .uid(userId)
                .uname(uname)
                .build();
        sendMsg(key, ACTION_MATCH_ENTER, 7, TimeUnit.DAYS, msg, SEND_LIMIT);
    }

    /**
     * 每日特训加积分
     *
     * @param userId
     * @param username
     * @param subjectId
     * @param practiceId
     * @throws BizException
     */
    private void sendDailyTrainMsg(long userId, String username, int subjectId, long practiceId) throws BizException {
        DayTrain dayTrain = dayTrainService.findCurrent(userId, subjectId);

        if (dayTrain.getFinishCount() == DAY_TRAIN_FINISH_COUNT) {
            sendDailySubmitMsg(userId, username, practiceId, ACTION_TRAIN_DAILY);
        }
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

        HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();

        if (String.valueOf(limit).equals(opsForHash.get(key, hashKey))) {
            return;
        }

        boolean exists = redisTemplate.hasKey(key);
        opsForHash.increment(key, hashKey, 1);

        if (!exists) {
            redisTemplate.expire(key, timeout, unit);
        }

        rabbitTemplate.convertAndSend("", MQ_NAME, msg);

        logger.info("send msg={},redis key={},hashkey={}", JsonUtil.toJson(msg), key, hashKey);
    }
}
