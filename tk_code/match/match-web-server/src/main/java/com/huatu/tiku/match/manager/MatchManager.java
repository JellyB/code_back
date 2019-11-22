package com.huatu.tiku.match.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.dao.document.PaperDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 描述：方法对match数据进行缓存处理
 *
 * @author biguodong
 * Create time 2019-01-08 上午11:36
 **/

@Slf4j
@Component
public class MatchManager {

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 模考大赛首页试卷信息查询缓存
     */
    Cache<Integer, List<Match>> MATCH_HEADER_LIST_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(300)
            .build();

    /**
     * 根据 subject 获取(在活动期间的)模考大赛列表
     *
     * @param subject
     * @return nullable
     */
    public List<Match> findMatchesBySubjectWithCache(int subject) throws BizException {
        StopWatch stopWatch = new StopWatch("查询首页模考大赛");
        try {
            stopWatch.start("isReadOpen");
            boolean readOpen = CacheSwitchManager.isReadOpen(subject);
            stopWatch.stop();
            if (readOpen) {
                stopWatch.start("走缓存guava++");
                List<Match> cacheList = MATCH_HEADER_LIST_CACHE.getIfPresent(subject);
                stopWatch.stop();
                if (null != cacheList) {
                    return cacheList;           //guava缓存有效
                }
            }
            //写入guava缓存
            Consumer<List<Match>> writeCache = ((matches -> {
                stopWatch.start("checkMatchCacheFlag准备写入缓存");
                boolean cacheFlag = CacheSwitchManager.checkMatchCacheFlag(matches, subject);
                stopWatch.stop();
                if (cacheFlag) {
                    stopWatch.start("写入guava缓存");
                    MATCH_HEADER_LIST_CACHE.put(subject, matches);
                    stopWatch.stop();
                }
            }));
            stopWatch.start("查询redis");
            //guava缓存过期，尝试从redis中获取数据
            String matchListKey = MatchInfoRedisKeys.getMatchListKey(subject);
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String tempMatchString = valueOperations.get(matchListKey);
            stopWatch.stop();
            if (null != tempMatchString) {
                try {
                    List<Match> matches = JsonUtil.toList(tempMatchString, Match.class);
                    writeCache.accept(matches);
                    return matches;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /**
             * 查询redis数据为空，已过期或者数据解析失败，则直接查询mongo更行redis缓存和guava缓存
             */
            stopWatch.start("查询mongo");
            List<Match> matches = findMatches(subject);
            Boolean flag = valueOperations.setIfAbsent(matchListKey, JsonUtil.toJson(matches));
            stopWatch.stop();
            if (flag) {     //redis插入时间有
                writeCache.accept(matches);
                stopWatch.start("写入redis缓存");
                redisTemplate.expire(matchListKey, 1, TimeUnit.HOURS);
                stopWatch.stop();
            }
            return matches;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            log.info(stopWatch.prettyPrint());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据 subject 获取(在活动期间的)模考大赛列表(通过mongo查询)
     *
     * @param subject
     * @return
     * @throws BizException
     */
    public List<Match> findMatches(int subject) throws BizException {
        StopWatch stopWatch = new StopWatch("-------findMatches");
        stopWatch.start("findMatchBySubject start");
        //科目下所有模考大赛
        List<Match> matches = matchDao.findMatchBySubject(subject);
        stopWatch.stop();
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        stopWatch.start("findBatchByIds start");
        //科目下活动区间的模考大赛试卷
        List<Paper> papers = paperDao.findBatchByIds(matches.stream().map(Match::getPaperId).collect(Collectors.toSet()));
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        if (CollectionUtils.isEmpty(papers)) {
            return Lists.newArrayList();
        }
        long currentTime = System.currentTimeMillis();
        //可用的matchIds
        List<Integer> matchIds = papers.stream()
                .filter(i -> i instanceof EstimatePaper)
                .filter(i -> {
                    long onlineTime = ((EstimatePaper) i).getOnlineTime();
                    long offlineTime = ((EstimatePaper) i).getOfflineTime();
                    return onlineTime <= currentTime && offlineTime >= currentTime;
                })
                .sorted(Comparator.comparing(i->((EstimatePaper) i).getOnlineTime()))
                .map(Paper::getId).collect(Collectors.toList());
        return matches.stream()
                .filter(i -> matchIds.contains(i.getPaperId()))
                .sorted(Comparator.comparing(i->matchIds.indexOf(i.getPaperId())))
                .collect(Collectors.toList());
    }

    public List<Match> findBySubjectAndTag(int subjectId, int tagId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String matchListKey = MatchInfoRedisKeys.getMatchListByTagKey(subjectId, tagId);
        String value = valueOperations.get(matchListKey);
        if (StringUtils.isNotBlank(value)) {
            try {
                return JsonUtil.toList(value, Match.class);
            } catch (Exception e) {
                log.error("json convert error,\"JsonUtil.toList(value,Match.class)\": value = {}", value);
                e.printStackTrace();
                redisTemplate.delete(matchListKey);
            }
        }
        List<Match> matches = matchDao.findBySubjectAndTag(subjectId, tagId);
        valueOperations.set(matchListKey, JsonUtil.toJson(matches));
        redisTemplate.expire(matchListKey, 1, TimeUnit.MINUTES);
        return matches;
    }

    public Match findById(int paperId) {
        Match match = matchDao.findById(paperId);
        return match;
    }
}
