package com.huatu.ztk.question.dao;

import com.huatu.ztk.question.bean.ReflectQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 映射表底层实现
 * Created by huangqingpeng on 2018/8/23.
 */
@Repository
public class ReflectQuestionDao {
    private static final Logger logger = LoggerFactory.getLogger(ReflectQuestionDao.class);
    /**
     * 存储试题的集合名字
     */
    //public static final String collection = "reflect_question";

    @Autowired
    private MongoTemplate mongoTemplate;
    public ReflectQuestion findById(int id) {
        return mongoTemplate.findById(id,ReflectQuestion.class);
    }

    public List<ReflectQuestion> findByIds(List<Integer> ids) {
        Criteria criteria = Criteria.where("oldId").in(ids);
        Query query = new Query(criteria);
        //logger.info("reflectQuestion query={}",query);
        return mongoTemplate.find(query, ReflectQuestion.class);
    }
}
