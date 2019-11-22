package com.huatu.tiku.match.dao.document;

import com.huatu.ztk.question.bean.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lijun on 2018/11/1
 */
@Repository
public class QuestionDao {

    private static final String COLLECTION_NAME = "ztk_question_new";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 通过ID查询试题信息
     *
     * @param id 试题ID
     * @return 试题信息
     */
    public Question findQuestionById(Integer id) {
        return mongoTemplate.findById(id, Question.class, COLLECTION_NAME);
    }

    /**
     * 通过ID 批量查询
     *
     * @param idList ID 合集
     * @return 试题信息
     */
    public List<Question> findQuestionById(List<Integer> idList) {
        final Criteria criteria = Criteria.where("_id").in(idList);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Question.class, COLLECTION_NAME);
    }
}
