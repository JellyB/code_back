package com.huatu.ztk.knowledge.service.v2.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeysV2;
import com.huatu.ztk.knowledge.dao.QuestionUserMetaDao;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.knowledge.service.RedisDebugService;
import com.huatu.ztk.knowledge.service.v1.QuestionFinishServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Slf4j
public class QuestionFinishServiceImplV2 implements QuestionFinishServiceV1 {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionUserMetaDao questionUserMetaDao;
    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    RedisDebugService debugService;

    @Override
    public int count(long uid, QuestionPoint questionPoint) {
        int pointId = questionPoint.getId();
        log.info("count:uid={},point={}", uid, pointId);
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String finishCountKey = RedisKnowledgeKeysV2.getFinishCountKey(uid);
        debugService.test.accept(finishCountKey);
        String finishCount = opsForHash.get(finishCountKey, String.valueOf(pointId));
        if (null != finishCount) {
            return Integer.parseInt(finishCount);
        }
        Map<Integer, Integer> countFinishQuestionMap = countAll2Redis(uid);
        Integer count = countFinishQuestionMap.getOrDefault(questionPoint.getId(), 0);
        return count;
    }

    @Override
    public Map<Integer, Integer> countAll(long uid) {

        String finishCountKey = RedisKnowledgeKeysV2.getFinishCountKey(uid);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, String> countMap = hashOperations.entries(finishCountKey);
        debugService.test.accept(finishCountKey);
        Map<Integer, Integer> countIntMap = countMap.entrySet().stream()
                .collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), (i -> Integer.parseInt(i.getValue()))));
        if (null != countIntMap && !countIntMap.isEmpty()) {
            return countIntMap;
        }

        return countAll2Redis(uid);
    }

    public Map<Integer, Integer> countAll2Redis(long uid) {
        String finishCountKey = RedisKnowledgeKeysV2.getFinishCountKey(uid);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<Integer, Integer> countFinishQuestionMap = questionUserMetaDao.countFinishQuestion(uid);
//        System.out.println("countFinishQuestionMap = " + JsonUtil.toJson(countFinishQuestionMap));
        if (null != countFinishQuestionMap && countFinishQuestionMap.size() > 0) {
            Map<String, String> countFinishQuestionStrMap = countFinishQuestionMap.entrySet().stream()
                    .collect(Collectors.toMap(pointMap -> String.valueOf(pointMap.getKey()),
                            pointMap -> String.valueOf(pointMap.getValue())));
            hashOperations.putAll(finishCountKey, countFinishQuestionStrMap);
            redisTemplate.expire(finishCountKey, 7, TimeUnit.DAYS);
        }
        return countFinishQuestionMap;
    }

    @Override
    public Map<Integer, Integer> countByPoints(List<Integer> points, long userId) {
        Map<Integer, Integer> finishCountMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(points)) {
            return finishCountMap;
        }
        log.info("count:uid={},point={}", userId, points);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String finishedCountKey = RedisKnowledgeKeysV2.getFinishCountKey(userId);
        debugService.test.accept(finishedCountKey);
        List<String> pointsStr = points.stream().map(String::valueOf).collect(Collectors.toList());
        List<String> countList = hashOperations.multiGet(finishedCountKey, pointsStr);
        if (countList != null &&
                !countList.isEmpty() &&
                countList.stream().filter(StringUtils::isNotBlank).findAny().isPresent()) {     //countList中至少要有一个是非空的
            finishCountMap = IntStream.range(0, pointsStr.size()).boxed().collect(Collectors
                    .toMap(j -> Integer.parseInt(pointsStr.get(j)), j -> NumberUtils.toInt(countList.get(j), 0)));
            return finishCountMap;
        }
        Map<Integer, Integer> countFinishQuestionMap = countAll2Redis(userId);
        return points.stream().collect(Collectors.toMap(i -> i, i -> countFinishQuestionMap.getOrDefault(i, 0)));
    }

    @Override
    public Set<String> filterQuestionIds(long uid, int pointId, Set<String> qids) {
        Set<String> members = getQuestionIds(uid, pointId);
        Collection<String> intersection = CollectionUtils.intersection(members, qids);
        //未做的试题列表
        return Sets.newHashSet(intersection);
    }

    @Override
    public Set<String> getQuestionIds(long uid, int pointId) {
        String finishedSetKey = RedisKnowledgeKeysV2.getFinishedSetKey(uid, pointId);
        debugService.test.accept(finishedSetKey);
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        try {
            //获取已经做过的列表
            Set<String> members = setOperations.members(finishedSetKey);
            if (CollectionUtils.isNotEmpty(members)) {
                return members;
            }
        } catch (Exception e) {
            log.error("ex", e);
        }
        QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        Set<Integer> finishQuestions = questionUserMetaDao.findFinishQuestion(uid, questionPoint);
        finishQuestions.add(-1);        //防止穿透查询
        setOperations.add(finishedSetKey, finishQuestions.stream().map(String::valueOf).toArray(String[]::new));
        redisTemplate.expire(finishedSetKey, 1, TimeUnit.DAYS);
        return finishQuestions.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    @Override
    public void clearRedisCache(long userId) {
        String wrongCountKey = RedisKnowledgeKeysV2.getFinishCountKey(userId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, String> countMap = hashOperations.entries(wrongCountKey);
        List<Integer> collect = countMap.entrySet().stream().map(Map.Entry::getKey).map(Integer::parseInt).collect(Collectors.toList());
        collect.forEach(i->redisTemplate.delete(RedisKnowledgeKeysV2.getFinishedSetKey(userId,i)));
        redisTemplate.delete(wrongCountKey);
    }


}
