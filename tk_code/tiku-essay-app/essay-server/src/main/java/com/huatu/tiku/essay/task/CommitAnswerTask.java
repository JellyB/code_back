package com.huatu.tiku.essay.task;


import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.AmswerCommitRedisKey;
import com.huatu.tiku.essay.service.UserAnswerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * 未批改成功的答题卡  再次提交
 * 5分钟一次增量提交，模考大赛的答题卡除外
 *
 * @author zhaoxi
 */
@Component
@Slf4j
public class CommitAnswerTask extends TaskService{
    private static final Logger logger = LoggerFactory.getLogger(CommitAnswerTask.class);
    @Autowired
    private UserAnswerService userAnswerService;

    private static final long TASK_LOCK_EXPIRE_TIME = 5;
    @Override
    public void run() {
        String serverIp = getLocalLock();
        logger.info("auto commit unfinished answerCard task start.server={}", serverIp);
        //提交五分钟前提交的且未完成的答题卡
        userAnswerService.unfinishedCardCommit();
    }

    @Scheduled(fixedRate = 60000 * 5)
    public void labelClose() throws BizException {
        task();
    }




    @Override
    public String getCacheKey() {
        return AmswerCommitRedisKey.getUnCommitAnswerLockKey();
    }

    @Override
    protected long getExpireTime() {
        return TASK_LOCK_EXPIRE_TIME;
    }
}
