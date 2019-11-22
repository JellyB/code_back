package com.huatu.tiku.teacher.service.impl.systems;

import com.google.common.collect.Maps;
import com.huatu.tiku.teacher.service.systems.QuestionPointService;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionPointServiceImpl implements QuestionPointService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map getPointQuestionCount() {
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map entries = hashOperations.entries(pointSummaryKey);
        if(MapUtils.isNotEmpty(entries)){
            entries.put("-1",redisTemplate.getExpire(pointSummaryKey, TimeUnit.MINUTES)+"");
        }
        return entries;
    }

    @Override
    public Object getPointQuestionIds() {
        long currentTimeMillis = System.currentTimeMillis();
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        HashOperations hashOperations = redisTemplate.opsForHash();
        Set<String> keys = hashOperations.keys(pointSummaryKey);
        Map<Integer, String> result = Maps.newHashMap();
        for (String key : keys) {
            String pointQuestionIds = RedisKnowledgeKeys.getPointQuesionIds(Integer.parseInt(key));
            Set<String> members = redisTemplate.opsForSet().members(pointQuestionIds);
            if (CollectionUtils.isNotEmpty(members)) {
                String ids = members.stream().collect(Collectors.joining(","));
                result.put(Integer.parseInt(key), ids);
            }
        }
        System.out.println("耗时："+(System.currentTimeMillis()-currentTimeMillis));
        return result;
    }
}
