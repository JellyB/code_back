package com.huatu.tiku.match.task;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.match.MatchRedisKeyConstant;
import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.question.bean.QuestionMeta;
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
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 试题统计信息持久化定时任务
 * ---用户行为数据集中在模考大赛期间，且只统计用户模考大赛行为数据
 * ---在模考大赛结束后，每分钟持久化缓存的数据到mysql，持续10分钟
 * Created by huangqingpeng on 2019/1/3.
 */
@Slf4j
@Component
public class QuestionMetaPersistenceTask {

    @Autowired
    private MatchDao matchDao;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;

    @Autowired
    RedisTemplate redisTemplate;

    public static long lockTime = 0;

    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
    }

    @Scheduled(fixedRate = 60000)
    public void questionMetaPersistence() throws BizException {
        try {
            if (!getLock()) {
                System.out.println("questionMetaPersistence error,getLock error");
                return;
            }

            /**
             * 所有在活动期间的考试
             */
            List<Match> currentMatchList = matchDao.findAll();
            if (CollectionUtils.isEmpty(currentMatchList)) {
                System.out.println("questionMetaPersistence error,currentMatchList is empty.");
                return;
            }
            /**
             * 排除未考试结束的考试
             */
            currentMatchList.removeIf(i -> i.getEndTime() + TimeUnit.MINUTES.toMillis(20) > System.currentTimeMillis());
            String lock = MatchInfoRedisKeys.getQuestionMetaLockedMatchId();
            Set<String> members = redisTemplate.opsForSet().members(lock);
            List<Integer> lockIds = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(members)){
              lockIds.addAll(members.stream().map(Integer::valueOf).collect(Collectors.toList()));
            }
            currentMatchList.removeIf(i -> lockIds.contains(i.getPaperId()));
            for (Match match : currentMatchList) {
                int matchPaperId = match.getPaperId();
                Example example = new Example(MatchQuestionMeta.class);
                example.and().andEqualTo("matchId", matchPaperId);
                int count = matchQuestionMetaService.selectCountByExample(example);
                if (count > 0) {
                    //统计信息只持久化一次，如果需要再次持久化，则删除以前的旧数据
                    continue;
                }
                boolean finished = matchUserMetaService.isFinished(matchPaperId);
                if (!finished) {
                    //未正式批改完成的考试，不做持久化
                    System.out.println(matchPaperId + " persistence error,it's unfinished.");
                    continue;
                }
                log.info("同步试题数据：paperId = {}",matchPaperId);
                Map<Integer, QuestionMeta> questionMetaMap = matchQuestionMetaService.getQuestionMetaByPaperId(matchPaperId);
                List<MatchQuestionMeta> metas = questionMetaMap.entrySet()
                        .stream().map(i -> MatchQuestionMeta.builder().matchId(matchPaperId)
                                .questionId(i.getKey())
                                .detail(JsonUtil.toJson(i.getValue()))
                                .build())
                        .collect(Collectors.toList());
                metas.forEach(matchQuestionMetaService::save);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            unlock();
            clearLockValue();
        }

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
            log.info("QuestionMetaPersistenceTask lock initd ,lockTime={}", lockTime);
        }
    }

    /**
     * 释放定时任务锁
     */
    private void unlock() {
        String lockKey = MatchInfoRedisKeys.getMatchQuestionMetaLockKey();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String lockValue = valueOperations.get(lockKey);

        log.info("current server={}", lockValue);
        if (getLockValue().equals(lockValue)) {
            redisTemplate.delete(lockKey);
            clearLockValue();
            log.info("release lock,lockValue={},timestamp={}", lockValue, System.currentTimeMillis());
        }
    }

    /**
     * @return 是否获得锁
     */
    private boolean getLock() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = MatchInfoRedisKeys.getMatchQuestionMetaLockKey();

        String value = opsForValue.get(lockKey);
        initLockValue();
        log.info("get lock timestamp={}", System.currentTimeMillis());
        if (StringUtils.isBlank(value)) { //值为空
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, getLockValue()).booleanValue();
            if (booleanValue) {
                redisTemplate.expire(lockKey, 2, TimeUnit.MINUTES);
            }
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
