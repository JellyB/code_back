package com.huatu.ztk.backend.paper.dao;

import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class PracticeSmartDao {
    private static final Logger logger = LoggerFactory.getLogger(PaperDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

//    public List<Question> find(int subject, int pointId, int difficult, int publish) {
//        Criteria criteria = Criteria.where("subject").is(subject)
//                .and("points").in(pointId)
//                .and("difficult").is(difficult)
//                .and("status").is(publish)
//                .and("type").is(QuestionType.SINGLE_CHOICE);
//        return mongoTemplate.find(new Query(criteria), Question.class);
//    }

//    public List<Question> find(int subject,int pointId,Set<Integer> difficults) {
//        Criteria criteria = Criteria.where("subject").is(subject)
//                .and("points").in(pointId)
//                .and("difficult").in(difficults)
//                .and("parent").is(0)
//                .and("status").in(QuestionStatus.AUDIT_SUCCESS,QuestionStatus.AUDIT_SUCCESS_NOT_ISSUED);
//        Query query = new Query(criteria);
//
//        query.with(new Sort(Sort.Direction.DESC, "_id"));
//
//        return mongoTemplate.find(query, Question.class);
//    }

    public List<Question> findAllPoints(int subject,Set<Integer> pointIds,Set<Integer> difficults) {
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("points").in(pointIds)
                .and("difficult").in(difficults)
                .and("parent").is(0)
                .and("status").in(QuestionStatus.AUDIT_SUCCESS,QuestionStatus.AUDIT_SUCCESS_NOT_ISSUED);
        Query query = new Query(criteria);

        return mongoTemplate.find(query, Question.class);
    }
}
