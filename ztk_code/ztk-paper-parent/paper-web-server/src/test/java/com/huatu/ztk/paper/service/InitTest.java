package com.huatu.ztk.paper.service;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.controller.InitPracticeController;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import sun.util.resources.cldr.ti.CalendarData_ti_ER;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaojieyue
 * Created time 2016-07-20 15:39
 */
public class InitTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(InitTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InitPracticeController initPracticeController;

    @Test
    public void aaTest(){
        Criteria criteria = Criteria.where("userId").is(11679964);
        Query query = new Query(criteria);
        query.limit(60);//最多取60
        final List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
        int all = 0;
        for (AnswerCard answerCard : answerCards) {
            all = all + answerCard.getWcount()+answerCard.getRcount();
        }
        System.out.println(all);
    }
}
