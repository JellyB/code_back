package com.huatu.ztk.report.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.question.bean.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class QuestionDao {
    private static final String collection = "ztk_question_new";

    Cache<Integer,Question> questionCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Question> findByIds(List<Integer> ids){
        Query query = new Query();
        List<Question> questions = ids.stream().map(i -> questionCache.getIfPresent(i)).filter(i -> null != i)
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(questions)){
            if(questions.size() == ids.size()){
                return questions;
            }
            ids.removeAll(questions.stream().map(Question::getId).collect(Collectors.toList()));
        }
        //通过id批量获取
        query.addCriteria(Criteria.where("id").in(ids));
        List<Question> tempList = mongoTemplate.find(query, Question.class, collection);
        questions.addAll(tempList);
        return questions;
    }
}
