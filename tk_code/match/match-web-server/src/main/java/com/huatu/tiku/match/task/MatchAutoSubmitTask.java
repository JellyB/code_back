package com.huatu.tiku.match.task;

import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.tiku.match.service.v1.paper.PaperUserMetaService;
import com.huatu.tiku.match.service.v1.reward.PaperRewardService;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MatchAutoSubmitTask {


    @Autowired
    private MatchDao matchDao;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    RedisTemplate redisTemplateWithoutServerName;

    @Autowired
    private AnswerCardDBService answerCardDBService;
    @Autowired
    private PaperUserMetaService paperUserMetaService;
    @Autowired
    private MetaHandlerService metaHandlerService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PaperRewardService paperRewardService;
    public static long lockTime = 0;

    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
    }

    @Scheduled(fixedRate = 60000)
    public void submitMatchAnswer() throws BizException {
        try {
            if (!getLock()) {
                return;
            }
            String matchSubmitAnswerCardIdSetKey = MatchInfoRedisKeys.getMatchSubmitAnswerCardIdSetKey();
            Long submittedSize = redisTemplate.opsForSet().size(matchSubmitAnswerCardIdSetKey);
            if(null == submittedSize || submittedSize > 0){
                return;
            }
            /**
             * 所有在活动期间的考试
             */
            List<Match> currentMatchList = matchDao.findUsefulMatch();
            if (CollectionUtils.isEmpty(currentMatchList)) {
                return;
            }
            /**
             * 排除未考试结束的考试
             */
            currentMatchList.removeIf(i -> i.getEndTime() > System.currentTimeMillis());
            for (Match currentMatch : currentMatchList) {
                int paperId = currentMatch.getPaperId();
//                log.info("自动交卷开始：paperId={},name={}", paperId, currentMatch.getName());
                String practiceIdSetKey = MatchInfoRedisKeys.getMatchPracticeIdSetKey(paperId);


                SetOperations setOperations = redisTemplateWithoutServerName.opsForSet();

                Set<Object> temps = setOperations.members(practiceIdSetKey);
                Set<String> practiceIdStrings = temps.stream().map(i -> {
                    if (i instanceof Long) {
                        return i.toString();
                    } else {
                        return String.valueOf(i);
                    }
                }).collect(Collectors.toSet());
                //答题卡id set为空或者模考大赛未结束
                if (CollectionUtils.isEmpty(practiceIdStrings)
                        || System.currentTimeMillis() < currentMatch.getEndTime() + TimeUnit.SECONDS.toMillis(150)) {
//                    log.info("自动交卷失败：答题卡位空：{}，模考大赛未结束：{}", CollectionUtils.isEmpty(practiceIdStrings),
//                            System.currentTimeMillis() < currentMatch.getEndTime() + TimeUnit.SECONDS.toMillis(150));
                    continue;
                }
//                log.error("practiceIdStrings={}", practiceIdStrings.stream().collect(Collectors.joining(",")));
                List<Long> practiceIds = practiceIdStrings.stream().map(Long::parseLong).collect(Collectors.toList());
                log.info("自动交卷的答题卡Id：{}", practiceIdStrings.stream().collect(Collectors.joining(",")));
                for (Long practiceId : practiceIds) {
                    StandardCard answerCard = (StandardCard) answerCardDBService.findById(practiceId);
                    long userId = answerCard.getUserId();
                    handlerAnswerCardSubmit(userId, practiceId, answerCard, setOperations, practiceIdSetKey);
                    sendSubmitMsg(userId, practiceId);
                }


                Long size = setOperations.size(practiceIdSetKey);
                if (size == 0) {
                    redisTemplateWithoutServerName.delete(practiceIdSetKey);
                }
//                log.info("自动交卷结束：paperId={},name={}", paperId, currentMatch.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            unlock();
            clearLockValue();
        }

    }

    private void sendSubmitMsg(long userId, Long practiceId) {
        try{
            String token = userSessionService.getTokenById(userId);
            String uname = userSessionService.getUname(token);
            paperRewardService.sendMatchSubmitMsg(userId, uname, practiceId);
        }catch (Exception e){
            log.error("交卷积分添加失败：userId={},practiceId={}",userId,practiceId);
            e.printStackTrace();
        }
    }

    /**
     * 处理答题卡的提交工作
     *
     * @param userId
     * @param practiceId
     * @param answerCard
     * @param setOperations
     * @param practiceIdSetKey
     */
    private void handlerAnswerCardSubmit(long userId, Long practiceId, StandardCard answerCard, SetOperations setOperations, String practiceIdSetKey) {
        //答题卡未提交
        if (answerCard.getStatus() != AnswerCardInfoEnum.Status.FINISH.getCode()) {
            log.info("auto submit,uid={},cardId={},card={}", userId, practiceId, JsonUtil.toJson(answerCard));
            try {
                answerCard.setStatus(AnswerCardInfoEnum.Status.FINISH.getCode());
                answerCardDBService.saveToDB(answerCard);
                metaHandlerService.saveScore(practiceId);
                paperUserMetaService.addFinishPractice(answerCard.getUserId(), answerCard.getPaper().getId(), practiceId);
            } catch (Exception e) {
                log.error("auto submit error", e);
            }
        }
        Long remove = setOperations.remove(practiceIdSetKey, practiceId + "");
        log.info("remove flag = {}", remove);
    }

    private void clearLockValue() {
        lockTime = 0;
    }

    /**
     * 本地地址
     *
     * @return
     */
    private static String getLockValue() {
        return lockTime + "";
    }

    /**
     * 初始化lockTime
     * lockTime == 0 表示本服务没有获取到定时任务锁
     * lockTime != 0 表示本服务有未处理的定时任务，建议直接返回，等待上次任务释放锁
     */
    private static void initLockValue() {
        if (lockTime == 0) {
            lockTime = System.currentTimeMillis() * RandomUtils.nextInt(1, 9);
            log.info("MatchAutoSubmitTask lock initd ,lockTime={}", lockTime);
        }
    }

    /**
     * 释放定时任务锁
     */
    private void unlock() {
        String lockKey = MatchRedisKeys.getMatchAutoSubmitLockKey();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String currentServer = valueOperations.get(lockKey);

        log.info("current server={}", currentServer);
        if (getLockValue().equals(currentServer)) {
            redisTemplate.delete(lockKey);
            clearLockValue();
            log.info("release lock,server={},timestamp={}", currentServer, System.currentTimeMillis());
        }
    }

    /**
     * @return 是否获得锁
     */
    private boolean getLock() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = MatchRedisKeys.getMatchAutoSubmitLockKey();

        String value = opsForValue.get(lockKey);
        initLockValue();
        log.info("get lock timestamp={}", System.currentTimeMillis());
        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, getLockValue()).booleanValue();
            redisTemplate.expire(lockKey, 5, TimeUnit.MINUTES);
            return booleanValue;
        } else if (StringUtils.isNoneBlank(value) && !value.equals(getLockValue())) {
            //被其它服务器锁定
            log.info("auto submit match lock server={},return", value);
            return false;
        } else { //被自己锁定
            return true;
        }
    }
}
