package com.huatu.ztk.backend.paper.dao;

import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by linkang on 3/6/17.
 */
@Repository
public class PaperQuestionDao {
    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 通过id查询
     * @param id
     * @return
     */
    public Question findQuestionById(int id){
        final Question question = mongoTemplate.findById(id, Question.class,"ztk_question_new");
        return question;
    }

    /**
     * 通过id查询扩展
     * @param qid
     * @return
     */
    public QuestionExtend findExtendById(int qid) {
        return mongoTemplate.findById(qid, QuestionExtend.class);
    }

    /**
     * 批量查询
     * @param qids
     * @return
     */
    public List<Question> findBath(List<Integer> qids) {
        Criteria criterial = Criteria.where("id").in(qids);
        return mongoTemplate.find(new Query(criterial), Question.class,"ztk_question_new");
    }

    /**
     * 批量查询扩展
     * @param qids
     * @return
     */
    public List<QuestionExtend> findExtendBath(List<Integer> qids) {
        Criteria criterial = Criteria.where("qid").in(qids);
        return mongoTemplate.find(new Query(criterial), QuestionExtend.class);
    }
}
