package com.huatu.ztk.knowledge.cacheTask.util;

import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 数据补偿策略,如果原始的key值失效,进行数据补偿
 * 由于 finishQuestion 存储在SSDB 中key为永久生效 不会出现失效的情况.
 * Created by junli on 2018/4/12.
 */
@Component
public class QuestionPersistenceDataGetUtil {
    private static final Logger log = LoggerFactory.getLogger(QuestionPersistenceDataGetUtil.class);

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 数据补偿策略
     *
     * @param key      查询的key
     * @param supplier 查询策略
     * @return 返回正常数据
     */
    public Set getCollectQuestionPersistence(String key, Supplier<Set> supplier) {
        return getQuestionPersistence(key, supplier, QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT);
    }

    /**
     * 数据补偿策略
     *
     * @param key      查询的key
     * @param supplier 查询策略
     * @return 返回正常数据
     */
    public Set getWrongQuestionPersistence(String key, Supplier<Set> supplier) {
        return getQuestionPersistence(key, supplier, QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG);
    }


    protected Set getQuestionPersistence(final String key, Supplier<Set> supplier, QuestionPersistenceEnum.TableName tableName) {
        Set set = supplier.get();
        if (null != set && set.size() != 0) {
            return set;
        }
        try {
            HashMap<String, String> map = QuestionPersistenceKeyUtil.cacheKeyToMap(key);
            if (MapUtils.isEmpty(map)) {
                return set;
            }
            //补偿策略 - 回写数据
            List<String> questionList = service.getQuestionIdByUserIdAndPointId(map.get("userId"), map.get("pointId"), tableName);
            if (null == questionList || questionList.size() == 0) {
                return set;
            }
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            long time = System.currentTimeMillis() / 1000 - 1000;
            for (int index = 0; index < questionList.size(); index++) {
                zSetOperations.add(key, questionList.get(index), time - index * 1000);
            }
            redisTemplate.expire(key, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
        } catch (Exception e) {
            //避免此处异常情况影响正常业务逻辑
            e.printStackTrace();
        } finally {
            //重置查询
            Set resultSet = supplier.get();
            return resultSet;
        }
    }
}
