package com.huatu.ztk.knowledge.service.v1.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.service.v1.QuestionFinishServiceV1;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionFinishServiceImplV1 implements QuestionFinishServiceV1 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionFinishServiceImplV1.class);
    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Override
    public int count(long uid, QuestionPoint questionPoint) {
        if (uid < 0 || null == questionPoint) {
            return 0;
        }
//        logger.info("count:uid={},point={}",uid,questionPoint.getId());
        final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        final String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(uid);
        final String pointStr = questionPoint.getId() + "";
        String finishCount = null;
        try {
            finishCount = connection.hget(finishedCountKey, pointStr);
//            logger.info("ssdb,finishedCountKey={},count={}",finishedCountKey,finishCount);
            if(StringUtils.isNotBlank(finishCount)){
                return Integer.parseInt(finishCount);
            }
        } catch (Exception e) {
            logger.error("ex", e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
        return 0;
    }

    @Override
    public Map<Integer,Integer> countAll(long userId) {
        final String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(userId);

        final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        try {
            final Map<String, String> stringMap = connection.hgetall(finishedCountKey);
            if (stringMap != null) {
                return stringMap.entrySet().stream().collect(Collectors.toMap(i->Integer.parseInt(i.getKey()),i->Integer.parseInt(i.getValue())));
            }
        } catch (Exception e) {
            logger.error("ex", e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
        return Maps.newHashMap();
    }

    @Override
    public Map<Integer,Integer> countByPoints(List<Integer> points, long userId) {
        final String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(userId);
        final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        Map<Integer, Integer> finishCountMap = new HashMap<>();
        try {
            Map<byte[], byte[]> map = connection.hgetByKeys(finishedCountKey.getBytes(), points.stream().map(String::valueOf).map(String::getBytes).collect(Collectors.toList()));
            final Map<String, String> stringMap = map.entrySet().stream().collect(Collectors.toMap(entry -> new String(entry.getKey()), entry -> new String(entry.getValue())));
            if (stringMap != null) {
                for (String key : stringMap.keySet()) {
                    finishCountMap.put(Integer.valueOf(key), Integer.parseInt(stringMap.get(key)));
                }
            }
        } catch (Exception e) {
            logger.error("ex", e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
        return finishCountMap;
    }

    @Override
    public Set<String> filterQuestionIds(long uid, int pointId, Set<String> qids) {
        final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        final String finishedSetKey = RedisKnowledgeKeys.getFinishedSetKey(uid, pointId);
        try {
            //获取已经做过的列表
            final List<String> collect = qids.stream().collect(Collectors.toList());
            final Set<String> finishedSet = connection.zget(finishedSetKey, collect).keySet();
            //未做的试题列表
            return finishedSet;
        } catch (Exception e) {
            logger.error("ex", e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
        return Sets.newHashSet();
    }

    @Override
    public Set<String> getQuestionIds(long uid, int pointId) {
        //TODO  暂不实现
        return Sets.newHashSet();
    }

    @Override
    public void clearRedisCache(long userId) {

    }
}
