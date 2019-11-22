package com.huatu.ztk.knowledge.dao;

import com.huatu.ztk.paper.bean.AnswerCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnswerCardDao {
    private static final Logger logger = LoggerFactory.getLogger(AnswerCardDao.class);

    private static final String collection = "ztk_answer_card";
    /**
     * 存储试题的集合名字
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询答题卡
     * @param id
     * @return
     */
    public AnswerCard findById(long id){
        return mongoTemplate.findById(id,AnswerCard.class);
    }

}
