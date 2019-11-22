package com.huatu.ztk.knowledge.service.v2.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeysV2;
import com.huatu.ztk.knowledge.dao.QuestionUserMetaDao;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.knowledge.service.RedisDebugService;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.huatu.ztk.knowledge.task.UserAnswersTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class QuestionErrorServiceImplV2 implements QuestionErrorServiceV1 {

    @Autowired
    private QuestionUserMetaDao questionUserMetaDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    @Qualifier("knowledgeServiceImplV2")
    private KnowledgeService knowledgeService;

    @Autowired
    private RedisDebugService debugService;
    private static final Logger logger = LoggerFactory.getLogger(UserAnswersTask.class);

    /**
     * 查询某一个知识点下试题数量
     *
     * @param uid
     * @param questionPoint
     * @return
     */
    @Override
    public int count(long uid, QuestionPoint questionPoint) {
        if (uid == 0 || null == questionPoint) {
            return 0;
        }
        // redis中查询 wrong_count_{uid}
        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(uid);
        debugService.test.accept(wrongCountKey);
        String pointId = String.valueOf(questionPoint.getId());
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String questionCount = hashOperations.get(wrongCountKey, pointId);
        if (StringUtils.isNotEmpty(questionCount)) {
            return Integer.parseInt(questionCount);
        }

        //mongo中查询,并且填充redis wrong_count_{uid}
        Map<Integer, Integer> allKnowQueCount = countAll2Redis(uid);
        Integer knowQuesCount = allKnowQueCount.getOrDefault(questionPoint.getId(), 0);
        return knowQuesCount;
    }


    @Override
    public Map<Integer, Integer> countAll(long uid) {
        //redis 中全量查询
        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(uid);
        debugService.test.accept(wrongCountKey);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, String> countMap = hashOperations.entries(wrongCountKey);
        Map<Integer, Integer> collect = countMap.entrySet().stream()
                .collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), (i -> Integer.parseInt(i.getValue()))));
        if (null != collect && collect.size() > 0) {
            return collect;
        }

        return countAll2Redis(uid);
    }

    /**
     * 用户新错题统计数据过期或者没有，补偿策略如下：
     * 1首先判断老的错题统计缓存的过期时间，确认是否同步数据到持久层，如果已做过持久化数据统计，则直接使用持久化数据生成统计数据
     * 2如果未左持久化数据统计，返回老缓存中数据，同时另启线程持久化错题数据到mongo
     *
     * @param uid
     * @return
     */
    private Map<Integer, Integer> countAll2Redis(long uid) {
        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(uid);
        debugService.test.accept(wrongCountKey);
        HashOperations hashOperations = redisTemplate.opsForHash();
        String oldKey = RedisKnowledgeKeys.getWrongCountKey(uid);
        Predicate<String> isExpire = (key -> {      //判断是否有过期时间
            Long expire = redisTemplate.getExpire(key);
            return null != expire && expire > 0 && expire <= TimeUnit.DAYS.toSeconds(360);
        });
        if (!isExpire.test(oldKey)) {     //如果没有过期时间
            try {
                Map<String, String> entries = redisTemplate.opsForHash().entries(oldKey);
                syncErrorIds2Mongo(uid, entries);
                return entries.entrySet().stream().collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), i -> Integer.parseInt(i.getValue())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //mongo中根据知识点分组查询 && 将结果集放入redis中
        Map<Integer, Integer> allKnowQueCount = questionUserMetaDao.countErrorQuestion(uid);
        if (null != allKnowQueCount && allKnowQueCount.size() > 0) {
            hashOperations.putAll(wrongCountKey, allKnowQueCount.entrySet().stream().collect(Collectors.toMap(i -> i.getKey().toString(), i -> i.getValue().toString())));
            redisTemplate.expire(wrongCountKey, 14, TimeUnit.DAYS);
        }
        return allKnowQueCount;

    }

    /**
     * 同步数据到mongo
     *
     * @param uid
     * @param entries
     */
    private void syncErrorIds2Mongo(final long uid, final Map<String, String> entries) {
        if (MapUtils.isEmpty(entries)) {
            return;
        }
        Runnable syncRunnable = new Runnable() {
            @Override
            public void run() {
                String lock = RedisKnowledgeKeys.getWrongCountKey(uid) + "lock";
                Boolean isGetLock = redisTemplate.opsForValue().setIfAbsent(lock, uid+"");
                try {
                    if (!isGetLock) {
                        return;
                    }
                    redisTemplate.expire(lock, 5, TimeUnit.MINUTES);
//                    System.out.println("同步错题本数据，开始:uid = " + uid + "entries = " + JsonUtil.toJson(entries));
                    /**
                     * 得到一级知识点信息
                     */
                    Example example = new Example(Knowledge.class);
                    example.and().andEqualTo("level", 1);
                    List<Knowledge> knowledges = knowledgeService.selectByExample(example);
                    if (CollectionUtils.isEmpty(knowledges)) {
                        return;
                    }
                    /**
                     * 筛选用户有错题的一级知识点
                     */
                    Set<Integer> firstIds = knowledges.stream().map(BaseEntity::getId)
                            .map(Long::intValue)
                            .filter(i -> entries.containsKey(i.toString()))
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(firstIds)) {
                        return;
                    }
                    /**
                     * 查询缓存错题数据
                     */
                    List<Integer> ids = Lists.newArrayList();
                    for (Integer firstId : firstIds) {
                        String wrongSetKey = RedisKnowledgeKeys.getWrongSetKey(uid, firstId);
                        Set<String> range = redisTemplate.opsForZSet().range(wrongSetKey, 0, -1);
                        if (CollectionUtils.isNotEmpty(range)) {
                            ids.addAll(range.stream().map(Integer::parseInt).collect(Collectors.toList()));
                        }
                    }
                    /**
                     * 更新mongo中的错题数据
                     */
                    if (CollectionUtils.isNotEmpty(ids)) {
                        questionUserMetaDao.clearErrorQuestions(uid, -1);
                        questionUserMetaDao.addErrorQuestions(uid, ids);
                    }

                    redisTemplate.expire(RedisKnowledgeKeys.getWrongCountKey(uid), 360, TimeUnit.DAYS);
                    Map<Integer, Integer> resultMap = questionUserMetaDao.countErrorQuestion(uid);
                    if (MapUtils.isNotEmpty(resultMap)) {
                        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(uid);
                        redisTemplate.opsForHash().putAll(wrongCountKey,
                                resultMap.entrySet().stream().collect(Collectors.toMap(i -> String.valueOf(i.getKey()), i -> String.valueOf(i.getValue()))));
                        redisTemplate.expire(wrongCountKey, 14, TimeUnit.DAYS);
                    }
//                    System.out.println("同步错题本数据，结束:uid = " + uid + "ids = " + JsonUtil.toJson(ids));
                } catch (Exception e) {
                    logger.error("持久化错题本数据失败～！！！");
                    e.printStackTrace();
                } finally {
                    if (isGetLock) {
                        redisTemplate.delete(lock);
                    }
                }
            }
        };
        new Thread(syncRunnable).start();

    }


    @Override
    public Set<Integer> getQuestionIds(Integer pointId, long uid) {
        //redis中获取某模块下试题ID集合
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongSetKey = RedisKnowledgeKeysV2.getWrongSetKey(uid, pointId);
        debugService.test.accept(wrongSetKey);
        Set ids = zSetOperations.range(wrongSetKey, 0, -1);
        if (CollectionUtils.isNotEmpty(ids)) {
            return transObject2Int(ids);
        }

        Set<Integer> questionIds = addKnowQuestionIdsToRedis(pointId, uid);
        return questionIds;
    }

    /**
     * 知识下的试题ID放入缓存
     *
     * @param pointId
     * @param uid
     * @return
     */
    public Set<Integer> addKnowQuestionIdsToRedis(Integer pointId, long uid) {
        QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        //mongo查询
        Set<Integer> errorQuestion = questionUserMetaDao.findErrorQuestion(uid, questionPoint);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        if (CollectionUtils.isNotEmpty(errorQuestion)) {
            String wrongSetKey1 = RedisKnowledgeKeysV2.getWrongSetKey(uid, pointId);
            Double score = new Double(System.currentTimeMillis());
            for (Integer questionId : errorQuestion) {
                zSetOperations.add(wrongSetKey1, questionId + "", score);
            }
            redisTemplate.expire(wrongSetKey1, 7, TimeUnit.DAYS);
            return errorQuestion;
        }
        return Sets.newHashSet();
    }

    @Override
    public Set<Integer> getQuestionIds(int pointId, long userId, int start, int end) {

        String wrongSetKey = RedisKnowledgeKeysV2.getWrongSetKey(userId, pointId);
        debugService.test.accept(wrongSetKey);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRange(wrongSetKey, start, end);
        logger.info("getQuestionIds:pointId={},userId={},set={}", pointId, userId, set);

        if (CollectionUtils.isNotEmpty(set)) {
            return transObject2Int(set);
        }
        Set<Integer> questionIds = addKnowQuestionIdsToRedis(pointId, userId);
        if (CollectionUtils.isEmpty(questionIds)) {
            return Sets.newHashSet();
        }
        int length = end - start + 1;
        if (questionIds.size() <= length) {
            return questionIds;
        } else {
            List<Integer> questions = questionIds.stream().collect(Collectors.toList()).subList(start, length);
            Set<Integer> result = new HashSet<>(questions);
            logger.info(" getQuestionIds 知识点:{},试题ID:{}", pointId, questions);
            return result;
        }

    }

    @Override
    public boolean isExist(long uid, int point, int questionId) {
        Set<Integer> questionIds = getQuestionIds(point, uid);
        return questionIds.contains(questionId);
    }

    /**
     * 删除试题
     *
     * @param uid
     * @param point
     * @param questionId
     */
    @Override
    public void deleteQuestion(long uid, int point, int questionId) {
        // 1.删除redis中 知识点下错误试题列表(1,2,3级都要删下) TODO 其他层级的需要删除
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String errorSetKey = RedisKnowledgeKeysV2.getWrongSetKey(uid, point);
        debugService.test.accept(errorSetKey);
        logger.info("errorSetKey 内容是:{}", errorSetKey);
        zSetOperations.remove(errorSetKey, questionId + "");

        //2.mongo中 errFlag置为0
        questionUserMetaDao.clearErrorQuestions(uid, questionId);

        //3.重置1,2,3级知识点下每级的试题数量
        Function<String, Long> getSize = (zSetOperations::size);
        restCount(uid, point, getSize);

    }

    @Override
    public void clearAll(long userId, int subject) {
        //获取所有的一二三级知识点，组成 List<QuestionPoint>
        final String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(userId);
        List<QuestionPoint> questionPoints = questionPointService.getQuestionPoints(subject);
        List<Integer> pointIds = questionPoints.stream().map(QuestionPoint::getId).collect(Collectors.toList());
        //遍历每个知识点,组装错题列表
        HashMap<String, String> map = Maps.newHashMap();
        ArrayList<String> errorSetKeys = Lists.newArrayList();
        for (Integer point : pointIds) {
            //做题模式，组装错题列表key
            String errorSetKey = RedisKnowledgeKeysV2.getWrongSetKey(userId, point);
            errorSetKeys.add(errorSetKey);
            //背题模式，组装错题列表key
            String wrongCursor = RedisKnowledgeKeysV2.getWrongCursor(userId, point);
            errorSetKeys.add(wrongCursor);
            map.put(point + "", String.valueOf(0));
        }

        //批量删除做题set，背题模式set
        redisTemplate.delete(errorSetKeys);

        //批量删除,将所有的知识点的数量重置为0
        redisTemplate.opsForHash().putAll(wrongCountKey, map);

        //mongo中errorFlag置为0
        questionUserMetaDao.clearErrorQuestions(userId, -1);
    }

    /**********************以下为背题模式处理**********************/

    /**
     * 背题模式下，移除某个试题
     *
     * @param uid
     * @param point
     * @param questionId
     */
    @Override
    public void deleteLookMode(long uid, Integer point, int questionId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongCursor = RedisKnowledgeKeysV2.getWrongCursor(uid, point);
        debugService.test.accept(wrongCursor);
        zSetOperations.remove(wrongCursor, questionId + "");
    }


    /**
     * 背题模式下，获取某个知识点下试题数量
     *
     * @param uid
     * @param pointId
     * @return
     */
    @Override
    public int countLookMode(long uid, int pointId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final String wrongCursorKey = RedisKnowledgeKeysV2.getWrongCursor(uid, pointId);
        Long reSizes = zSetOperations.size(wrongCursorKey);
        debugService.test.accept(wrongCursorKey);
        if (null == reSizes) {
            return 0;
        }
        return reSizes.intValue();
    }

    /**
     * 将做题模式下的错题Set 复制到 背题模式下的错题Set
     *
     * @param uid     用户ID
     * @param pointId 知识点ID
     * @param total   总量
     */
    @Override
    public void copyWrongSetToCursor(long uid, int pointId, int total) {
        String errorSetKey = RedisKnowledgeKeysV2.getWrongSetKey(uid, pointId);
        String wrongCursorKey = RedisKnowledgeKeysV2.getWrongCursor(uid, pointId);
        debugService.test.accept(errorSetKey);
        debugService.test.accept(wrongCursorKey);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> set = zSetOperations.reverseRangeWithScores(errorSetKey, 0, total - 1);
        if (CollectionUtils.isNotEmpty(set)) {
            logger.info("set size = {}", set.size());
            for (ZSetOperations.TypedTuple<String> tuple : set) {
                zSetOperations.add(wrongCursorKey, tuple.getValue(), tuple.getScore());
            }
        }


    }

    /**
     * 背题模式,根据知识点查询试题ID
     *
     * @param uid      用户ID
     * @param pointId  知识点ID
     * @param tempSize
     * @return
     */
    @Override
    public List<Integer> getQuestionIdsLookMode(long uid, int pointId, Integer tempSize) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String wrongCursorKey = RedisKnowledgeKeysV2.getWrongCursor(uid, pointId);
        debugService.test.accept(wrongCursorKey);
        Set ids = zSetOperations.reverseRange(wrongCursorKey, 0, tempSize - 1);
        List<Integer> tempIds = transObject2Int(ids).stream().collect(Collectors.toList());
        List<String> collect = tempIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            logger.info("delete ids ={}", collect);
            zSetOperations.remove(wrongCursorKey, collect.toArray());
        }
        return tempIds;
    }

    @Override
    public List<QuestionPointTree> queryErrorPointTrees(long userId, int subject) {
        final String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(userId);
        debugService.test.accept(wrongCountKey);
        countAll(userId);
        return questionPointService.findCountPointTrees(wrongCountKey, subject, true);
    }

    @Override
    public void clearRedisCache(long userId) {
        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(userId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, String> countMap = hashOperations.entries(wrongCountKey);
        List<Integer> collect = countMap.entrySet().stream().map(Map.Entry::getKey).map(Integer::parseInt).collect(Collectors.toList());
        collect.forEach(i->redisTemplate.delete(RedisKnowledgeKeysV2.getWrongSetKey(userId,i)));
        redisTemplate.delete(wrongCountKey);
    }

    @Override
    public void checkErrorPointRedis(long userId, int subject) {
        return;
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
     * 重置用户某个知识点下的错题数量
     *
     * @param uid
     * @param point
     * @param getSize
     */
    private void restCount(long uid, int point, Function<String, Long> getSize) {
        String errorSetKey = RedisKnowledgeKeysV2.getWrongSetKey(uid, point);
        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(uid);
        debugService.test.accept(errorSetKey);
        debugService.test.accept(wrongCountKey);
        Long errorSize = getSize.apply(errorSetKey);
        redisTemplate.opsForHash().put(wrongCountKey, point + "", String.valueOf(errorSize));
    }
}
