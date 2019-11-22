package com.huatu.ztk.report.service;

import com.google.common.base.Optional;
import com.huatu.ztk.report.bean.PowerSummary;
import com.huatu.ztk.report.bean.QuestionSummary;
import com.huatu.ztk.report.common.RedisReportKeys;
import com.huatu.ztk.report.dubbo.PowerSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 21:28
 */
public class PowerSummaryDubboServiceImpl implements PowerSummaryDubboService {
    private static final Logger logger = LoggerFactory.getLogger(PowerSummaryDubboServiceImpl.class);

    @Resource(name = "redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private QuestionSummaryService questionSummaryService;
    /**
     * 查询用户能力汇总
     *
     * @param userId  用户id
     * @param subject 科目
     * @param area    区域
     * @return
     */
    @Override
    public PowerSummary find(long userId, int subject, int area) {
        long t1 = System.currentTimeMillis();
        final QuestionSummary questionSummary = questionSummaryService.findByUserId(userId, subject);
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //保存用户名和分数的set
        String userScoreZsetKey = RedisReportKeys.getUserScoreZsetKey(area,subject);
        final String userIdStr = String.valueOf(userId);
        //计算用户平均分,+1方式0除
        int avgScore = 100*questionSummary.getRcount()/(questionSummary.getAcount()+1);

        //设置该用户的分数，需要每次设置，排名时使用
        zSetOperations.add(userScoreZsetKey,userIdStr,avgScore);

        final Long size = Math.max(zSetOperations.size(userScoreZsetKey),1);
        Double score = Optional.fromNullable(zSetOperations.score(userScoreZsetKey, userId+"")).or(0d);
        Long rank = Optional.fromNullable(zSetOperations.rank(userScoreZsetKey, userIdStr)).or(0L);

        logger.info("redisTemplate.opsForZSet expendTime={}", System.currentTimeMillis() - t1);
        PowerSummary powerSummary = PowerSummary.builder()
                .avg(0)
                .beat(100*(rank+1)/size)
                .rank(size.intValue() - rank.intValue())
                .score(score)
                .uid(userId)
                .subject(subject)
                .build();
        /**
         * update by lijun 2018-06-05
         * if(rank == 0 ) 100*(rank + 1)/size ,当 size <100 的时候 beat > 1
         * 矫正排名为0的情况 beat 错误
         */
        if (0 == rank|| 0D==score.doubleValue()){
            powerSummary.setBeat(0);
        }
        
        return powerSummary;
    }
}
