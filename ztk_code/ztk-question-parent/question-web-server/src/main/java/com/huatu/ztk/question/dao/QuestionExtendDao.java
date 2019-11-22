package com.huatu.ztk.question.dao;

import com.huatu.ztk.question.bean.QuestionExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue
 * Created time 2017-02-17 09:33
 */
@Repository
public class QuestionExtendDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionExtendDao.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    public QuestionExtend findById(int qid) {
        return mongoTemplate.findById(qid,QuestionExtend.class);
    }
}
