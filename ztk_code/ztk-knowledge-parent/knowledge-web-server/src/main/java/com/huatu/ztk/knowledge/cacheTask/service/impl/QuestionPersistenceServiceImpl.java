package com.huatu.ztk.knowledge.cacheTask.service.impl;

import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by junli on 2018/3/19.
 */
@Service
public class QuestionPersistenceServiceImpl implements QuestionPersistenceService {
    private static final Logger log = LoggerFactory.getLogger(QuestionPersistenceServiceImpl.class);

    @Autowired
    private QuestionPersistenceDao questionPersistenceDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Override
    public void cacheAddUserActionFromRedis(String redisKey, QuestionPersistenceEnum.RedisKey cacheRedisKey,boolean updateTTL) {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.add(cacheRedisKey.getRedisKey(), redisKey);
        if (updateTTL){
            redisTemplate.expire(redisKey, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
        }
    }

    @Override
    public void cacheDeleteUserActionFromRedis(String[] redisKey, QuestionPersistenceEnum.RedisKey cacheRedisKey) {
        if (ArrayUtils.isEmpty(redisKey)) {
            return;
        }
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.remove(cacheRedisKey.getRedisKey(), redisKey);
    }

    @Override
    public List<String> getWrongCacheData(String key) {
        return getListFromZSetRedis(key);
    }

    @Override
    public List<String> getCollectCacheData(String key) {
        return getListFromZSetRedis(key);
    }

    /**
     * 从 redis 中获取 zset 数据逆序排列
     *
     * @param key redis key
     * @return
     */
    private List<String> getListFromZSetRedis(String key) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<String> set = zSetOperations.range(key, 0, -1);
        if (null == set) {
            return Collections.emptyList();
        }
        ArrayList<String> list = new ArrayList<>(set);
        Collections.reverse(list);
        return list;
    }

    @Override
    public void save(QuestionPersistenceModel model, QuestionPersistenceEnum.TableName tableName) {
        if (null == model || StringUtils.isAnyEmpty(model.getUserId(), model.getQuestionPointId())) {
            return;
        }
        if (StringUtils.isEmpty(model.getQuestionId())) {
            //如果当前的考题ID 没有值,则直接删除此数据
            questionPersistenceDao.deletePhysics(model.getUserId(), model.getQuestionPointId(), tableName);
        } else {
            questionPersistenceDao.save(model.getUserId(), model.getQuestionPointId(), model.getQuestionId(), tableName);
        }
    }

    @Override
    public List<String> getQuestionIdByUserIdAndPointId(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName) {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(questionPointId)) {
            return null;
        }
        return questionPersistenceDao.getQuestionIdByUserIdAndPointId(userId, questionPointId, tableName);
    }

       @Override
    public List<String> getFinishCacheData(final String key) {
        final ArrayList<String> list = new ArrayList<>();
        Consumer<SsdbConnection> consumer = (connection) -> {
            Map<String, String> map = connection.zscan(key, "", (double) 0, (double) System.currentTimeMillis(), 100 * 10000);
            List<String> collect = map.keySet().stream().collect(Collectors.toList());
            if (null != collect) {
                list.addAll(collect);
            }
        };
        getSSDBConnectionAndDoSomething(consumer, (exception) ->
                //打印日志
                log.info("QuestionPersistenceService >> 获取SSDB 连接异常,key:{},异常信息:{}", key, exception.getMessage())
        );
        return list;
    }

    /**
     * 处理redis中是否有此key(redis中是否有此key,如果没有,直接将mysql写入redis)
     * 更新时间以缓存时间90天为界限,只刷新90天之前的数据
     */
    @Override
    public void judgeIsOutOfExpiredTime(long userId, List<String> collectCacheData, String pointId, String collectSetKey, QuestionPersistenceEnum.TableName tableName, QuestionPersistenceModel model) {
        //更新时间是否在10号之前
        if (CollectionUtils.isEmpty(collectCacheData)) {
            this.writeDataToRedis(collectSetKey, userId, pointId, tableName, false);
        } else {
            if (null != model.getUpdateTime()) {
                Date startTime = model.getUpdateTime();
                Instant instant = startTime.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate startLocalDate = instant.atZone(zoneId).toLocalDate();
                long until = startLocalDate.until(LocalDate.now(), ChronoUnit.DAYS);
                if (until > 90) {
                    this.writeDataToRedis(collectSetKey, userId, pointId, tableName, true);
                }
            }
        }
    }


    /**
     * @param key       reids key
     * @param userId    用户ID
     * @param pointId   知识点ID
     * @param tableName 表名
     * @param flag      是否刷新此key数据到redis
     */
    private void writeDataToRedis(String key, Long userId, String pointId, QuestionPersistenceEnum.TableName tableName, Boolean flag) {

        //补偿策略 - 回写数据
        List<String> questionList = getQuestionIdByUserIdAndPointId(userId.toString(), pointId, tableName);
        List<String> questionListResult = new ArrayList<>(questionList);
        if (null == questionList || questionList.size() == 0) {
            return;
        }
        if (flag) {
            List<String> questionsFromRedis = getCollectCacheData(key);
            questionListResult.addAll(questionsFromRedis);
        }

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        long time = System.currentTimeMillis() / 1000 - 1000;
        for (int index = 0; index < questionList.size(); index++) {
            zSetOperations.add(key, questionList.get(index), time - index * 1000);
        }
        redisTemplate.expire(key, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
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

}
