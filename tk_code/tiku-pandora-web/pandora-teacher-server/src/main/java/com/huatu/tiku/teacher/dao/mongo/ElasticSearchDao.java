package com.huatu.tiku.teacher.dao.mongo;

import com.huatu.ztk.question.bean.GenericQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author jbzm
 * @date 2018/7/20 1:35 PM
 **/
@Repository
public class ElasticSearchDao {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ElasticSearchDao(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public GenericQuestion genericQuestion() {
        mongoTemplate.findAll(GenericQuestion.class);
        return null;
    }
}