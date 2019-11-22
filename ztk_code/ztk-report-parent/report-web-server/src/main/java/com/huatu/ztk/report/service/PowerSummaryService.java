package com.huatu.ztk.report.service;

import com.google.common.base.Optional;
import com.huatu.ztk.report.bean.PowerSummary;
import com.huatu.ztk.report.common.RedisReportKeys;
import com.huatu.ztk.report.dubbo.PowerSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by shaojieyue
 * Created time 2016-06-03 15:02
 */

@Service
public class PowerSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(PowerSummaryService.class);

    @Autowired
    private PowerSummaryDubboService powerSummaryDubboService;

    public PowerSummary find(long userId, int subject, int area){
        return powerSummaryDubboService.find(userId, subject, area);
    }
}
