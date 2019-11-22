package com.huatu.ztk.knowledge.service.v1.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeysV2;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.knowledge.task.UserQuestionPointCheckTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionErrorServiceImplV1 implements QuestionErrorServiceV1 {

    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorServiceImplV1.class);
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private QuestionPersistenceUtil questionPersistenceUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 查询单个知识点错题量
     *
     * @param uid
     * @param questionPoint
     * @return
     */
    @Override
    public int count(long uid, QuestionPoint questionPoint) {
        if (uid < 0 || null == questionPoint) {
            return 0;
        }
//        logger.info("count:uid={},point={}", uid, questionPoint.getId());
        try {
            String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid);
            String pointStr = questionPoint.getId() + "";
            String wrongCount = (String) redisTemplate.opsForHash().get(wrongCountKey, pointStr);
//            logger.info("count={}", wrongCount);
            if(StringUtils.isNotBlank(wrongCount)){
                return Integer.parseInt(wrongCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<Integer, Integer> countAll(long uid) {
        final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(wrongCountKey);
//        logger.info("countAll:key={},entries={}", wrongCountKey, entries);
        try {
            return entries.entrySet().stream().collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), i -> Integer.parseInt(i.getValue())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }

    @Override
    public Set<Integer> getQuestionIds(Integer pointId, long uid) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, pointId);
        Set ids = zSetOperations.range(wrongSetKey, 0, -1);
        logger.info("getQuestionId:pointId={},uid={},questionIds={}",pointId,uid,ids);
        return transObject2Int(ids);
    }


    /**
     * 查询单个知识点的错题ID
     *
     * @param pointId
     * @param userId
     * @param end
     * @return
     */
    @Override
    public Set<Integer> getQuestionIds(int pointId, long userId, int start, int end) {
        String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(userId, pointId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRange(wrongSetKey, start, end);
//        logger.info("getQuestionIds:pointId={},userId={},set={}", pointId, userId, set);
        return transObject2Int(set);
    }

    private Set<Integer> transObject2Int(Set set) {

        HashSet<Integer> result = Sets.newHashSet();
        if (CollectionUtils.isEmpty(set)) {
            return result;
        }
        try {
            for (Object o : set) {
                if (null != o && NumberUtils.isDigits(o.toString())) {
                    result.add(Integer.parseInt(o.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 判断错题是否是在用户特定知识点下
     *
     * @param uid
     * @param point
     * @param questionId
     * @return
     */
    @Override
    public boolean isExist(long uid, int point, int questionId) {
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, point);
        Double score = zSetOperations.score(errorSetKey, questionId + "");
        if (null != score && score.intValue() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public void deleteQuestion(long uid, int point, int questionId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, point);
        zSetOperations.remove(errorSetKey, questionId + "");
        Function<String, Long> getSize = (zSetOperations::size);
        restCount(uid, point, getSize);

    }

    @Override
    public void deleteLookMode(long uid, Integer point, int questionId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongCursor = RedisKnowledgeKeys.getWrongCursor(uid, point);
        zSetOperations.remove(wrongCursor, questionId + "");
    }

    @Override
    public int countLookMode(long uid, int pointId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final String wrongCursorKey = RedisKnowledgeKeys.getWrongCursor(uid, pointId);
        Long reSizes = zSetOperations.size(wrongCursorKey);
        if (null == reSizes) {
            return 0;
        }
        return reSizes.intValue();
    }

    @Override
    public void copyWrongSetToCursor(long uid, int pointId, int total) {
        String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, pointId);
        String wrongCursorKey = RedisKnowledgeKeys.getWrongCursor(uid, pointId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> set = zSetOperations.reverseRangeWithScores(errorSetKey, 0, total - 1);
        if (CollectionUtils.isNotEmpty(set)) {
            logger.info("set size = {}", set.size());
            for (ZSetOperations.TypedTuple<String> tuple : set) {
                zSetOperations.add(wrongCursorKey, tuple.getValue(), tuple.getScore());
            }
        }
    }

    @Override
    public List<Integer> getQuestionIdsLookMode(long uid, int pointId, Integer tempSize) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongCursorKey = RedisKnowledgeKeys.getWrongCursor(uid, pointId);
        Set ids = zSetOperations.reverseRange(wrongCursorKey, 0, tempSize - 1);
        List<Integer> tempIds = transObject2Int(ids).stream().collect(Collectors.toList());
        List<String> collect = tempIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
//            logger.info("delete ids ={}", collect);
            zSetOperations.remove(wrongCursorKey, collect.toArray());
        }
        return tempIds;
    }

    @Override
    public List<QuestionPointTree> queryErrorPointTrees(long userId, int subject) {
        //TODO 需要修改
        final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId);
        return questionPointService.findCountPointTrees(wrongCountKey, subject, true);
    }

    @Override
    public void clearRedisCache(long userId) {
        return;
    }

    @Override
    public void checkErrorPointRedis(long userId, int subject) {
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId) + "_check";      //试题检查错题本的分布式锁
        Boolean checkFlag = redisTemplate.opsForValue().setIfAbsent(wrongCountKey, userId + "");
        if (checkFlag) {
            logger.info("用户需要例行检查错题本数据,userId={}", userId);
            redisTemplate.expire(wrongCountKey, 7, TimeUnit.DAYS);
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("userId",userId);
            map.put("type", UserQuestionPointCheckTask.CHECK_WRONG);
            map.put("subject", subject);
            rabbitTemplate.convertAndSend("","check_user_question_point",map);
        }
    }


    @Override
    public void clearAll(long userId, int subject) {
        final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId);
        List<QuestionPoint> questionPoints = questionPointService.getQuestionPoints(subject);
        List<Integer> pointIds = questionPoints.stream().map(QuestionPoint::getId).collect(Collectors.toList());
        //对每个知识点都进行处理
        HashMap<String, String> map = Maps.newHashMap();
        ArrayList<String> errorSetKeys = Lists.newArrayList();
        for (Integer point : pointIds) {
            //做错的
            String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(userId, point);
            errorSetKeys.add(errorSetKey);
            String wrongCursor = RedisKnowledgeKeys.getWrongCursor(userId, point);
            errorSetKeys.add(wrongCursor);
            map.put(point + "", String.valueOf(0));
            /**
             * 缓存需要持化的key 值信息
             * add by lijun 2018-03-20
             */
            questionPersistenceUtil.addWrongQuestionPersistence(errorSetKey);
        }
        redisTemplate.delete(errorSetKeys);
        redisTemplate.opsForHash().putAll(wrongCountKey, map);
    }

    /**
     * 重置用户某个知识点下的错题数量
     *
     * @param uid
     * @param point
     * @param getSize
     */
    private void restCount(long uid, int point, Function<String, Long> getSize) {
        String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, point);
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid);
        Long errorSize = getSize.apply(errorSetKey);
        redisTemplate.opsForHash().put(wrongCountKey, point + "", String.valueOf(errorSize));
    }
}
