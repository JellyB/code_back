package com.huatu.ztk.paper.task;

import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.service.MatchService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class MatchAutoSubmitTaskV2 {
    private static final Logger logger = LoggerFactory.getLogger(MatchAutoSubmitTaskV2.class);

    @Autowired
    private MatchService matchService;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private UserDubboService userDubboService;

    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }

    @Scheduled(fixedRate = 60000)
    public void submitMatchAnswer() throws BizException{
        if (!getLock()) {
            return;
        }

        logger.info("auto submit match answer task start.server={}", getServerIp());
        for (int subjectId : matchDao.findSubjects()) {
            List<Match> currentMatchList = matchDao.findUsefulMatch(subjectId);
            if (CollectionUtils.isEmpty(currentMatchList)) {
                continue;
            }

            for(Match currentMatch:currentMatchList){
                autoSubmit(currentMatch);
            }
        }


        unlock();
        logger.info("auto submit match answer task end.server={}", getServerIp());
    }

    private static String getServerIp() {
        return System.getProperty("server_ip");
    }

    /**
     * 释放定时任务锁
     */
    private void unlock() {
        String lockKey = MatchRedisKeys.getMatchAutoSubmitLockKey();
        String currentServer = redisTemplate.opsForValue().get(lockKey);

        logger.info("current server={}",currentServer);
        if (getServerIp().equals(currentServer)) {
            redisTemplate.delete(lockKey);

            logger.info("release lock,server={},timestamp={}",currentServer,System.currentTimeMillis());
        }
    }

    /**
     *
     * @return 是否获得锁
     */
    private boolean getLock() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = MatchRedisKeys.getMatchAutoSubmitLockKey();

        String value = opsForValue.get(lockKey);

        logger.info("get lock timestamp={}",System.currentTimeMillis());
        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, getServerIp()).booleanValue();
            if(booleanValue){
                redisTemplate.expire(lockKey,5,TimeUnit.MINUTES);
            }
            return booleanValue;
        } else if (StringUtils.isNoneBlank(value) && !value.equals(getServerIp())) {
            //被其它服务器锁定
            logger.info("auto submit match lock server={},return", value);
            return false;
        } else { //被自己锁定
            return true;
        }
    }


    public void autoSubmit(Match currentMatch){
        int paperId = currentMatch.getPaperId();
        String practiceIdSetKey = MatchRedisKeys.getMatchPracticeIdSetKey(paperId);

        SetOperations<String, String> opsForSet = redisTemplate.opsForSet();

        Set<String> practiceIdStrings = opsForSet.members(practiceIdSetKey);

        //答题卡id set为空或者模考大赛未结束
        if (CollectionUtils.isEmpty(practiceIdStrings)
                || System.currentTimeMillis() < currentMatch.getEndTime() + TimeUnit.SECONDS.toMillis(150)) {
            return;
        }

        List<Long> practiceIds = practiceIdStrings.stream().map(Long::new).collect(Collectors.toList());

        for (Long practiceId : practiceIds) {
            StandardCard answerCard = (StandardCard) answerCardDao.findById(practiceId);
            long userId = answerCard.getUserId();

            UserDto userDto = userDubboService.findById(userId);

            //答题卡未提交
            if (answerCard.getStatus() != AnswerCardStatus.FINISH && userDto != null) {
                logger.info("auto submit,uid={},cardId={},card={}", userId, practiceId, JsonUtil.toJson(answerCard));
                try {
                    //交卷
                    matchService.submitMatchesAnswers(practiceId, userId, new ArrayList<>(), AreaConstants.QUAN_GUO_ID, userDto.getName());
                    //更新或添加练习统计数据
                    paperUserMetaService.addFinishPractice(userId, paperId, practiceId);
                } catch (Exception e) {
                    logger.error("auto submit error",e);
                }
            }
            opsForSet.remove(practiceIdSetKey, practiceId + "");
        }


        Long size = opsForSet.size(practiceIdSetKey);
        if (size == 0) {
            redisTemplate.delete(practiceIdSetKey);
        }
    }
}
