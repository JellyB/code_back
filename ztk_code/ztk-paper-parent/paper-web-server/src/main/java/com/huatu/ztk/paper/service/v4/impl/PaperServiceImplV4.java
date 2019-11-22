package com.huatu.ztk.paper.service.v4.impl;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.common.SmallRedisKey;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.service.v4.PaperServiceV4;
import io.netty.util.internal.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/2/13.
 */
@Service
public class PaperServiceImplV4 implements PaperServiceV4 {
    private final static Logger logger = LoggerFactory.getLogger(PaperServiceImplV4.class);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINESE);

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<EstimatePaper> getTodaySmallEstimatePaper(int subject) {
        long current = System.currentTimeMillis();
        String todayKey = dateFormat.format(new Date());
        String todaySmallEstimate = SmallRedisKey.getTodaySmallEstimate(subject, todayKey);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String value = valueOperations.get(todaySmallEstimate);
        if (null != value) {
            if (StringUtil.EMPTY_STRING.equals(value)) {
                return Lists.newArrayList();
            } else {
                try {
                    return JsonUtil.toList(value, EstimatePaper.class);
                } catch (Exception e) {
                    logger.error("redis缓存解析失败，value={},class=EstimatePaper", value);
                    redisTemplate.delete(todaySmallEstimate);
                }
            }
        }
        return getTodaySmallEstimatePaperByDB(subject, current, todaySmallEstimate, valueOperations);
    }

    @Override
    public Paper findById(int id) {
        return paperDao.findById(id);
    }

    @Override
    public String findAllCourseId(int subject) {
        String todaySmallEstimate = SmallRedisKey.getSmallEstimateCourseIds(subject);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        try {
            String value = valueOperations.get(todaySmallEstimate);
            if (null != value) {
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
            redisTemplate.delete(todaySmallEstimate);
        }
        return getSmallCourseIdsByDB(subject, todaySmallEstimate, valueOperations);
    }

    /**
     * 查mongo库获取小模考课程ID信息，并写入缓存
     *
     * @param subject
     * @param todaySmallEstimate
     * @param valueOperations
     * @return
     */
    private String getSmallCourseIdsByDB(int subject, String todaySmallEstimate, ValueOperations<String, String> valueOperations) {
        List<EstimatePaper> estimatePapers = paperDao.findEstimatePaperByType(subject, PaperType.SMALL_ESTIMATE);
        if (CollectionUtils.isNotEmpty(estimatePapers)) {
            String ids = estimatePapers.stream().map(EstimatePaper::getCourseId).filter(i -> i != 0).map(String::valueOf).collect(Collectors.joining(","));
            valueOperations.set(todaySmallEstimate, ids, 1, TimeUnit.MINUTES);
            return ids;
        }
        valueOperations.set(todaySmallEstimate, StringUtil.EMPTY_STRING, 1, TimeUnit.MINUTES);
        return StringUtil.EMPTY_STRING;

    }

    /**
     * 查mongo库获取试卷信息，并写入缓存
     *
     * @param subject
     * @param current
     * @param todaySmallEstimate
     * @param valueOperations
     * @return
     */

    private List<EstimatePaper> getTodaySmallEstimatePaperByDB(int subject, long current, String todaySmallEstimate, ValueOperations<String, String> valueOperations) {
        List<EstimatePaper> estimatePapers = paperDao.findEstimatePaperByTypeAndTime(subject, PaperType.SMALL_ESTIMATE, current);
        if (CollectionUtils.isNotEmpty(estimatePapers)) {
            valueOperations.set(todaySmallEstimate, JsonUtil.toJson(estimatePapers), 1, TimeUnit.MINUTES);
            return estimatePapers;
        }
        valueOperations.set(todaySmallEstimate, StringUtil.EMPTY_STRING, 1, TimeUnit.MINUTES);
        return Lists.newArrayList();
    }
}
