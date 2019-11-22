package com.huatu.ztk.knowledge.cacheTask.task;

import com.huatu.ztk.question.bean.ReflectQuestion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用以刷新 reflectQuestion 信息至缓存中
 * 更新策略为：系统启动时刷新一次，每天凌晨一点刷新一次
 * Created by lijun on 2018/9/7
 */
//@Component
public class ReflectQuestionInitTask implements InitializingBean {

    /**
     * 存在于关联表中的数据
     */
    public static final String REFLECT_QUESTION_CACHE_SET = "_cache:reflect:question:set";

    /**
     * 存储对应的关联关系
     */
    public static final String REFLECT_QUESTION_CACHE_HASH = "_cache:reflect:question:hash";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    /**
     * 项目后 刷新一遍缓存信息
     */
    @Override
    public void afterPropertiesSet() {
        init();
    }

    /**
     * 每天凌晨1点更新一遍 缓存数据
     */
    @Scheduled(cron = "0 0 1 ? * *")
    public void task() {
        init();
    }

    /**
     * 把 mongoDB 中 reflect_question 中的信息刷入缓存中
     */
    public void init() {
        Criteria criteria = Criteria.where("status").is(1);
        Query query = new Query(criteria);
        List<ReflectQuestion> reflectQuestions = mongoTemplate.find(query, ReflectQuestion.class, "reflect_question");
        //刷入所有的set 信息
        String[] questionIds = reflectQuestions.parallelStream()
                .map(ReflectQuestion::getOldId)
                .map(String::valueOf)
                .collect(Collectors.toCollection(ArrayList::new))
                .toArray(new String[]{});
        setOperations.add(REFLECT_QUESTION_CACHE_SET, questionIds);
        //刷入所有的对应关系信息
        HashMap<String, String> map = new HashMap<>();
        reflectQuestions.forEach(reflectQuestion -> {
            try {
                map.put(
                        String.valueOf(reflectQuestion.getOldId()),
                        String.valueOf(reflectQuestion.getNewId())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        hashOperations.putAll(REFLECT_QUESTION_CACHE_HASH, map);
    }

}
