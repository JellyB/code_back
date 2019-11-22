package com.huatu.ztk.question.dao;

import com.huatu.ztk.question.bean.QuestionNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue on 5/3/16.
 */
@Repository
public class QuestionNoteDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionNoteDao.class);

    /**
     * 存储笔记的集合名字
     */
    public static final String collection = "ztk_question_note";
    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(QuestionNote questionNote){
        logger.info("save question note,data={}",questionNote);
        mongoTemplate.save(questionNote,collection);
    }



}
