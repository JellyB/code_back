package com.huatu.ztk.knowledge.service.userCheck;


import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/3/12
 * @描述 处理数据行为数据（错题,收藏,做题记录）丢失
 * @描述 2018年3月和2018年6月进行过数据刷新和调整, 2018年9月题库重构后, 当时只是刷新了一个月的50万活跃用户的数据到redis中
 * 所以会出现用户2018年6月之后，未登陆app，导致用户行为数据丢失的情况
 * @描述 此方法会从mysql中将用户的做题记录, 重新刷新到redis集合中, 只是暂时使用。
 * 后面hqp会整合一个完整的方法
 */
@Service
public class SendQuestionToRedisService {

    private static final Logger logger = LoggerFactory.getLogger(CleanAllDataService.class);

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPersistenceDao questionPersistenceDao;

    public void mergeUserActionDataAndRedis(Long userId, Set<Integer> questionIds) {
        QuestionPersistenceEnum.TableName wrongTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG;
        final List<QuestionPersistenceModel> wrongQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(),
                wrongTable);
        if (CollectionUtils.isEmpty(wrongQuestionPersistenceModels)) {
            return;
        }
        Set<Integer> collect = wrongQuestionPersistenceModels.stream().filter(model -> {
            if (null != model.getUpdateTime()) {
                Date startTime = model.getUpdateTime();
                Instant instant = startTime.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate startLocalDate = instant.atZone(zoneId).toLocalDate();
                long until = startLocalDate.until(LocalDate.now(), ChronoUnit.DAYS);
                if (until > 90) {
                    return true;
                }
            }
            return false;
        }).map(QuestionPersistenceModel::getQuestionId)
                .flatMap(i -> Arrays.stream(i.split(",")))
                .parallel()
                .filter(NumberUtils::isDigits)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        questionIds.addAll(collect);
    }

    /**
     * 重新生成错题本树
     *  @param questions
     * @param oldMap
     * @param userId
     */
    public Set<String> restRedis(List<Question> questions, Map<Integer, Integer> oldMap, Long userId) {
        //组装每个知识点下的试题ID
        HashMap<Integer, Set<Integer>> pointQuestionMap = questions.stream()
                .filter(question -> null != question)
                .filter(question -> question instanceof GenericQuestion)
                .map(i -> (GenericQuestion) i)
                .filter(question -> question.getPoints().size() >= 3)
                .collect(
                        () -> new HashMap<Integer, Set<Integer>>(),
                        (map, question) -> {
                            List<Integer> points = question.getPoints().subList(0, 3);
                            int id = question.getId();
                            for (Integer point : points) {
                                Set<Integer> ids = map.getOrDefault(point, Sets.newHashSet());
                                ids.add(id);
                                map.put(point,ids);
                            }
                        },
                        (a, b) -> {
                            for (Map.Entry<Integer, Set<Integer>> entry : b.entrySet()) {
                                Set<Integer> ids = a.getOrDefault(entry.getKey(), Sets.newHashSet());
                                ids.addAll(entry.getValue());
                                a.put(entry.getKey(),ids);
                            }
                        }
                );
        System.out.println("pointQuestionMap = " + JsonUtil.toJson(pointQuestionMap));
        //对涉及的知识点ID进行遍历，先删除原试题数据，再补充新试题数据
        Set<Integer> ids = Sets.newCopyOnWriteArraySet(oldMap.keySet());
        ids.addAll(pointQuestionMap.keySet());
        Set<String> results = Sets.newHashSet();
        for (Integer id : ids) {
            String wrongKey = RedisKnowledgeKeys.getWrongSetKey(userId, id);
            results.add(wrongKey);
            if(oldMap.containsKey(id)){
                redisTemplate.delete(wrongKey);
            }
            if(pointQuestionMap.containsKey(id)){
                List<String> questionIds = pointQuestionMap.get(id).stream().map(String::valueOf).collect(Collectors.toList());
                ZSetOperations zSetOperations = redisTemplate.opsForZSet();
                long time = System.currentTimeMillis() / 1000 - 1000;
                for (int index = 0; index < questionIds.size(); index++) {
                    zSetOperations.add(wrongKey, questionIds.get(index), time - index * 1000);
                }
                redisTemplate.expire(wrongKey, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
            }
        }
        //删除原试题数量hash,重新写入新试题数据hash
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId);
        Map<String, String> collect = pointQuestionMap.entrySet().stream().collect(Collectors.toMap(i -> i.getKey().toString(), i -> String.valueOf(i.getValue().size())));
        System.out.println("wrongCountKey = " + wrongCountKey);
        System.out.println("collect = " + JsonUtil.toJson(collect));
        redisTemplate.delete(wrongCountKey);
        redisTemplate.opsForHash().putAll(wrongCountKey,collect);
        return results;
    }

}
