package com.huatu.tiku.teacher.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.teacher.dao.mongo.AnswerCardDao;
import com.huatu.tiku.teacher.service.paper.PaperMetaService;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.mongodb.BasicDBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 试卷统计信息处理逻辑实现
 * Created by huangqingpeng on 2019/1/15.
 */
@Slf4j
@Service
public class PaperMetaServiceImpl implements PaperMetaService {

    @Autowired
    AnswerCardDao answerCardDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Object syncMetaCache() {
        long index = 0;
        int limit = 10000;
        LongAdder size = new LongAdder();
        long total = 0L;
        Map<String,List<String>> redisMap = Maps.newHashMap();
        while(true){
            List<Map<String, Object>> answerCards = answerCardDao.findForPaperMeta(index, limit);
            if(CollectionUtils.isEmpty(answerCards)){
                break;
            }
            index = answerCards.stream().map(i->MapUtils.getLong(i,"id")).max(Long::compare).get();
            handerAnswerCard(answerCards,size,redisMap);
            total += answerCards.size();
            System.out.println("total = " + total);
        }
        log.info("answerCard's size = {} ",total);
        log.info("metas's size = {} ",size.longValue());
        log.info("redisMap.size={}",redisMap.size());
        Map<String, Integer> collect = redisMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().size()));
        for (Map.Entry<String, List<String>> entry : redisMap.entrySet()) {
            addRedisCache(entry.getKey(),entry.getValue());
        }
        return collect;
    }

    private void addRedisCache(String key, List<String> value) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        byte[][] bytes = value.stream().map(id -> id.getBytes()).collect(Collectors.toList())
                .toArray(new byte[][]{});
        connection.sAdd(key.getBytes(), bytes);
    }

    private void handerAnswerCard(List<Map<String, Object>> answerCards, LongAdder size, Map<String, List<String>> redisMap) {
        for (Map<String, Object> answerCard : answerCards) {
            Integer type = MapUtils.getInteger(answerCard, "type");
            Long id = MapUtils.getLong(answerCard, "id");
            Object paper = MapUtils.getObject(answerCard, "paper");
            if(paper instanceof BasicDBObject){
                Object paperId = ((BasicDBObject) paper).get("_id");
                if(null == paperId){
                    continue;
                }
                size.increment();
                putPaperRedisMap(id,Integer.parseInt(String.valueOf(paperId)),type,redisMap);
            }
        }
    }

    private void putPaperRedisMap(Long practiceId, int paperId, Integer type, Map<String, List<String>> redisMap) {
        String paperSubmitKey = PaperRedisKeys.getPaperSubmitKey(paperId, type);
        List<String> ids = redisMap.getOrDefault(paperSubmitKey, Lists.newArrayList());
        ids.add(practiceId.toString());
        redisMap.put(paperSubmitKey,ids);
    }


}
