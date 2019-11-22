package com.huatu.ztk.backend.question.dao;

import com.huatu.ztk.backend.question.bean.QuestionTemp;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-12  20:22 .
 */
@Repository
public class QuestionOperateDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDao.class);

    public static final String collection = "ztk_question_new";//题库表
    public static final String collectionExtend = "question_extend";//扩展表
    public static final String collectionId = "question_id_base";//存放试题id的表
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int findId(int num){
        Criteria criteria = Criteria.where("id").is(1);
        Query query = new Query(criteria);
        Update update = new Update().inc("questionId",num);
        int id = -1;
        QuestionTemp questionTemp = mongoTemplate.findAndModify(query,update,QuestionTemp.class,collectionId);
        id = questionTemp.getQuestionId();
        return id;
    }

    /**
     * 插入试题
     * @param question
     */
    public int insert(Question question){
        long startInsert = System.currentTimeMillis();
        mongoTemplate.save(question, collection);
        int id = question.getId();//获取id
        long endInsert = System.currentTimeMillis();
        logger.info("试题id={}，插入试题用时={}",id,endInsert-startInsert);
        return id;
    }

    /**
     * 批量插入试题
     * @param questionList
     */
    public void insertAll(List<Question> questionList){
        long startInsert = System.currentTimeMillis();
        mongoTemplate.insertAll(questionList);
        long endInsert = System.currentTimeMillis();
        logger.info("批量插入试题用时={}",endInsert-startInsert);
    }


    /**
     * 插入试题扩展部分
     * @param questionExtend
     */
    public void insertExtend(QuestionExtend questionExtend){
        long startInsert = System.currentTimeMillis();
        mongoTemplate.save(questionExtend,collectionExtend);
        long endInsert = System.currentTimeMillis();
        logger.info("插入扩展表用时={}",endInsert-startInsert);
    }

    /**
     * 批量插入试题扩展表
     * @param questionExtendList
     */
    public void insertExtendAll(List<QuestionExtend> questionExtendList){
        long startInsert = System.currentTimeMillis();
        mongoTemplate.insertAll(questionExtendList);
        long endInsert = System.currentTimeMillis();
        logger.info("批量插入扩展表用时={}",endInsert-startInsert);
    }
}
