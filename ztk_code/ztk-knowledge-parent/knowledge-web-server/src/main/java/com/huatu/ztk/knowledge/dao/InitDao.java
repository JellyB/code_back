package com.huatu.ztk.knowledge.dao;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-13  20:33 .
 */
@Repository
public class InitDao {

    public static final String collection = "ztk_question_new";
    public static final String collectionPaper = "ztk_paper";
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据questionId，查询该行测题
     * @param questionId
     * @return
     */
    public List<GenericQuestion> findById(int questionId,int num){
        List<Integer> status = new ArrayList<>();
        status.add(1);
        status.add(2);
        List<Integer> types = Arrays.asList(99,100,109);
        Criteria criteria = Criteria.where("id").gt(questionId).and("type").in(types).and("status").in(status)
                .and("year").gte(2008).and("parent").is(0).and("mode").is(1);
        Query query = new Query(criteria).limit(num);
        List<GenericQuestion> questions = mongoTemplate.find(query,GenericQuestion.class,collection);
        return questions;
    }

    /**
     * 根据questionId，查询该行测题
     * @param questionId
     * @return
     */
    public GenericQuestion findById(int questionId){
        GenericQuestion question = mongoTemplate.findById(questionId, GenericQuestion.class, collection);
        return question;
    }

    /**
     * 查询试题
     * @param questionId
     * @return
     */
    public Question findQuestionById(int questionId){
        return mongoTemplate.findById(questionId, Question.class, collection);
    }

    /**
     * 根据pid，查询该试卷
     * @param pid
     * @return
     */
    public Paper findPaperById(int pid){
        System.out.println(mongoTemplate.count(new Query(),collectionPaper));
        Paper paper = mongoTemplate.findById(pid, Paper.class, collectionPaper);
        return paper;
    }

    /**
     * 根据id查询答题卡
     * @param practiceId
     * @return
     */
    public AnswerCard findById(long practiceId) {
        final AnswerCard answerCard = mongoTemplate.findById(practiceId, AnswerCard.class);
        return answerCard;
    }
}
