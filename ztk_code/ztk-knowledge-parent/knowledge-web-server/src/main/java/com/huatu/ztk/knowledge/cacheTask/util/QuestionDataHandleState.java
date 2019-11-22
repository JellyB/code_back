package com.huatu.ztk.knowledge.cacheTask.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.task.ReflectQuestionInitTask;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据清洗
 * Created by lijun on 2018/8/26
 */
@Component
public class QuestionDataHandleState {

    private static final Logger log = LoggerFactory.getLogger(QuestionDataHandleState.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> opsForZSet;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> opsForHash;

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    //缓存 一二级 知识点情况
    private static final Cache<Integer, Set<String>> QUESTION_POINT = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.HOURS)//缓存时间
            .maximumSize(1000)
            .build();

    /**
     * 清理缓存信息
     *
     * @param model         试题缓存对象
     * @param cacheRedisKey key 类型
     * @param tableName     数据表名称
     */
    public void questionDataHandle(QuestionPersistenceModel model, QuestionPersistenceEnum.RedisKey cacheRedisKey, QuestionPersistenceEnum.TableName tableName) {
        if (QuestionPersistenceEnum.CLEAN_DATA) {
            String questionId = model.getQuestionId();
            if (StringUtils.isBlank(questionId)) {
                return;
            }
            List<QuestionPoint> pointList = questionPointDubboService.findParent(Integer.valueOf(model.getQuestionPointId()));
            if (null == pointList || pointList.size() != 3) {
                //此处必须是 3级、2级、1级 信息的集合
                //为了保证数据各个节点相加起来最终的一致性，采用从 三级节点累加至一级的方式。
                return;
            }
            List<Integer> hasDeletedQuestionList = handleModel(model);
//            if (CollectionUtils.isNotEmpty(hasDeletedQuestionList)) {
            //处理缓存信息
            switch (cacheRedisKey) {
                case QUESTION_USER_CACHE_WRONG:
                    questionDataCleanWrongAndCollect(model, hasDeletedQuestionList, RedisKnowledgeKeys::getWrongSetKey, RedisKnowledgeKeys::getWrongCountKey);
                    break;
                case QUESTION_USER_CACHE_COLLECT:
                    questionDataCleanWrongAndCollect(model, hasDeletedQuestionList, RedisKnowledgeKeys::getCollectSetKey, RedisKnowledgeKeys::getCollectCountKey);
                    break;
                case QUESTION_USER_CACHE_FINISH:
                    questionDataCleanFinished(model, hasDeletedQuestionList, RedisKnowledgeKeys::getFinishedSetKey, RedisKnowledgeKeys::getFinishedCountKey);
                    break;
                default:
                    return;
//                }
            }
        }
        //处理数据库实体库信息
        service.save(model, tableName);
    }

    /**
     * 处理model
     *
     * @param model
     * @return 被移除的数据
     */
    private List<Integer> handleModel(QuestionPersistenceModel model) {
        List<Integer> questionIdList = Arrays.stream(model.getQuestionId().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        List<Integer> validQuestionId = validQuestionId(questionIdList, Integer.valueOf(model.getQuestionPointId()));
        //获取已经被删除的试题集合
        List<Integer> hasDeletedQuestionList = questionIdList.parallelStream()
                .filter(oldId -> validQuestionId.parallelStream().noneMatch(validateId -> validateId.equals(oldId)))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(hasDeletedQuestionList)) {
            return Lists.newArrayList();
        }
        String validQuestionIdStr = "";
        if (CollectionUtils.isNotEmpty(validQuestionId)) {
            validQuestionIdStr = validQuestionId.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        //1.更新需要持久化的 question信息，后续持久化时候处理该值
        model.setQuestionId(validQuestionIdStr);
        return hasDeletedQuestionList;
    }

    /**
     * 处理 错题、收藏题目 -- 只处理三级节点 然后累计 一二级节点
     * 错题、收藏 存放在 redis zSet 中，所有的节点都有数据值
     * 此处只需要修改 缓存的值，数据库的值交由持久化时处理
     */
    private void questionDataCleanWrongAndCollect(
            QuestionPersistenceModel model,
            List<Integer> hasDeletedQuestionList,
            BiFunction<Long, Integer, String> getZSetKey,
            Function<Long, String> getHashKey) {
        List<QuestionPoint> pointList = questionPointDubboService.findParent(Integer.valueOf(model.getQuestionPointId()));
        final Long userId = Long.valueOf(model.getUserId());
        //2.处理 缓存中的 questionId 信息错误
        String zSetKey = getZSetKey.apply(userId, Integer.valueOf(model.getQuestionPointId()));
        String[] hasDeletedQuestionArray = hasDeletedQuestionList.stream().map(String::valueOf).collect(Collectors.toList())
                .toArray(new String[hasDeletedQuestionList.size()]);
        if (hasDeletedQuestionArray.length > 0) {
            opsForZSet.remove(zSetKey, hasDeletedQuestionArray);
        }
        //3.处理 总数错误情况，这里使用
        //使用 size 保证 数量和 缓存中的一致
        String afterValidNum = String.valueOf(opsForZSet.size(zSetKey));
        if (afterValidNum.equals("0")) {
            opsForHash.delete(getHashKey.apply(userId), model.getQuestionPointId());
        } else {
            opsForHash.put(getHashKey.apply(userId), model.getQuestionPointId(), afterValidNum);
        }

        /**
         * 处理一二级节点信息
         */
        Consumer<QuestionPoint> consumer = (questionPoint) -> {
            if(null == questionPoint){
                return;
            }
            //获取所有的子节点
            HashSet<Integer> childrenIdSet = Sets.newHashSet();
            if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {
                childrenIdSet.addAll(questionPoint.getChildren());
            } else if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_ONE) {
                questionPoint.getChildren().forEach(childrenId -> {
                    Set<Integer> threadChildrenSet = questionPointDubboService.findChildren(childrenId).stream()
                            .map(threadChildren -> threadChildren.getId())
                            .collect(Collectors.toSet());
                    childrenIdSet.addAll(threadChildrenSet);
                });
            }
            if (CollectionUtils.isEmpty(childrenIdSet)) {
                return;
            }
            //获取用户有数据的子节点信息 - 更新当前的父节点信息
            Set<ZSetOperations.TypedTuple<String>> allChildrenId = opsForHash.entries(getHashKey.apply(userId))
                    .keySet().stream()
                    .map(Integer::valueOf)
                    .filter(key -> childrenIdSet.contains(key))//获取有数据 且需要更新的节点信息 此处可能存在并发问题
                    .flatMap(key -> opsForZSet.rangeWithScores(getZSetKey.apply(userId, key), 0, -1).stream())
                    .collect(Collectors.toSet());
            //处理set值
            String userPointKey = getZSetKey.apply(userId, questionPoint.getId());
            redisTemplate.delete(userPointKey);
            //处理hash 值
            if (CollectionUtils.isEmpty(allChildrenId)) {
                opsForHash.delete(getHashKey.apply(userId), String.valueOf(questionPoint.getId()));
            } else {
                opsForHash.put(getHashKey.apply(userId), String.valueOf(questionPoint.getId()), allChildrenId.size() + "");
                allChildrenId.forEach(tuple -> opsForZSet.add(userPointKey,tuple.getValue(),tuple.getScore()));
                //opsForZSet.add(userPointKey, allChildrenId);
            }
        };
        //处理二级知识点
        consumer.accept(pointList.get(1));
        //处理一级知识点
        consumer.accept(pointList.get(2));
    }

    /**
     * 处理 已完成数据 -- 只处理三级节点 然后累加 一二级节点
     * 已完成数据 存放在SSDB 中
     */
    private void questionDataCleanFinished(
            QuestionPersistenceModel model,
            List<Integer> hasDeletedQuestionList,
            BiFunction<Long, Integer, String> getZSetKey,
            Function<Long, String> getHashKey) {
        List<QuestionPoint> pointList = questionPointDubboService.findParent(Integer.valueOf(model.getQuestionPointId()));
        final String finishedCountKey = getHashKey.apply(Long.valueOf(model.getUserId()));
        //处理三级知识点
        Consumer<SsdbConnection> ssdbConnectionConsumer = (connection) -> {
            String zSetKey = getZSetKey.apply(Long.valueOf(model.getUserId()), Integer.valueOf(model.getQuestionPointId()));
            //删除多余的试题
            String[] hasDeletedQuestionArray = hasDeletedQuestionList.stream().map(String::valueOf).collect(Collectors.toList())
                    .toArray(new String[hasDeletedQuestionList.size()]);
            if (hasDeletedQuestionArray.length > 0){
                connection.zdel(zSetKey, hasDeletedQuestionArray);
            }
            //修正试题个数 - 需要多级逐个修正
            String afterValidNum = String.valueOf(connection.zsize(zSetKey));
            //修正三级知识点数量
            if (afterValidNum.equals("0")) {
                connection.hdel(finishedCountKey, model.getQuestionPointId());
            } else {
                connection.hset(finishedCountKey, model.getQuestionPointId(), afterValidNum);
            }
        };
        //异常信息
        Consumer<Exception> exceptionConsumer = (exception) -> log.info("QuestionDataHandleState >>处理收藏信息异常 ,异常信息:{}", exception.getMessage());
        getSSDBConnectionAndDoSomething(ssdbConnectionConsumer, exceptionConsumer);

        //处理二级 一级知识点信息
        Consumer<SsdbConnection> parentConsumer = (connection) -> {
            for (int index = 1; index < pointList.size(); index++) {
                List<String> childList = pointList.get(index).getChildren().stream().map(String::valueOf).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {//child为空不处理,此处主要是防止以前的数据污染
                    continue;
                }
                final Map<String, String> resultMap = connection.hget(finishedCountKey.getBytes(), childList);
                final int finishCount = resultMap.values().stream().filter(count -> count != null).mapToInt(Integer::valueOf).sum();
                if (finishCount > 0) {
                    connection.hset(finishedCountKey, String.valueOf(pointList.get(index).getId()), String.valueOf(finishCount));
                } else {
                    connection.hdel(finishedCountKey, String.valueOf(pointList.get(index).getId()));
                }
            }
        };
        getSSDBConnectionAndDoSomething(parentConsumer, exceptionConsumer);
    }

    /**
     * 获取连接信息
     *
     * @param consumer          正常连接后的 消费行为
     * @param exceptionConsumer 异常后的 消费行为
     */
    private void getSSDBConnectionAndDoSomething(Consumer<SsdbConnection> consumer, Consumer<Exception> exceptionConsumer) {
        SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
        try {
            consumer.accept(connection);
        } catch (Exception e) {
            exceptionConsumer.accept(e);
        } finally {
            ssdbPooledConnectionFactory.returnConnection(connection);
        }
    }

    /**
     * 验证某个知识点下的试题ID信息 只获取当前有用的试题ID
     *
     * @param baseQuestionList 待验证的试题合集
     * @param questionPointId  试题节点信息
     * @return 过滤之后的节点信息，此处可以选择不返回
     */
    private List<Integer> validQuestionId(List<Integer> baseQuestionList, Integer questionPointId) {
        List<String> translateList = baseQuestionList.stream().map(String::valueOf).collect(Collectors.toList());
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        Set members = getQuestionInfoByPointId(questionPointId);
        if (CollectionUtils.isEmpty(members)) {
            return Lists.newArrayList();
        }
        if (members.containsAll(translateList)) {
            return baseQuestionList;
        }
        Set<String> intersect = setOperations.intersect(ReflectQuestionInitTask.REFLECT_QUESTION_CACHE_SET, translateList);
        List<Integer> result = translateList.parallelStream()
                .filter(baseQuestionId -> members.contains(baseQuestionId) || intersect.contains(baseQuestionId))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 查询一个知识点下所有的 试题信息
     * 后续逻辑修改，此处理论上只会进入三级节点
     *
     * @param questionPointId 知识点ID
     * @return
     */
    public Set<String> getQuestionInfoByPointId(Integer questionPointId) {
        DebugCacheUtil.showCacheContent(QUESTION_POINT, "QUESTION_POINT");
        Set<String> members = Sets.newHashSet();
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        //Note: 由于缓存中为存储 一二级 知识点与试题关联set 此处需要单独处理。
        QuestionPoint baseQuestionPoint = questionPointDubboService.findById(questionPointId);
        if (baseQuestionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
            //如果是三级节点
            members.addAll(setOperations.members(RedisKnowledgeKeys.getPointQuesionIds(questionPointId)));
        } else if (baseQuestionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {
            //如果不是三级节点
            Set<String> cacheData = QUESTION_POINT.getIfPresent(questionPointId);
            if (CollectionUtils.isNotEmpty(cacheData)) {
                members.addAll(cacheData);
            } else {
                List<QuestionPoint> children = questionPointDubboService.findChildren(questionPointId);
                children.forEach(questionPoint ->
                        members.addAll(setOperations.members(RedisKnowledgeKeys.getPointQuesionIds(questionPoint.getId())))
                );
                QUESTION_POINT.put(questionPointId, members);
            }

        } else if (baseQuestionPoint.getLevel() == QuestionPointLevel.LEVEL_ONE) {
            Set<String> cacheData = QUESTION_POINT.getIfPresent(questionPointId);
            if (CollectionUtils.isNotEmpty(cacheData)) {
                members.addAll(cacheData);
            } else {
                List<QuestionPoint> twoChildren = questionPointDubboService.findChildren(questionPointId);
                twoChildren.forEach(twoQuestionPoint -> {
                    List<QuestionPoint> children = questionPointDubboService.findChildren(twoQuestionPoint.getId());
                    children.forEach(questionPoint ->
                            members.addAll(setOperations.members(RedisKnowledgeKeys.getPointQuesionIds(questionPoint.getId())))
                    );
                });
                QUESTION_POINT.put(questionPointId, members);
            }
        }
        return members;
    }
}
