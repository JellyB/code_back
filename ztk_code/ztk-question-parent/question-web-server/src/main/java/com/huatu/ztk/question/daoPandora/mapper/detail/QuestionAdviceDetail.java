package com.huatu.ztk.question.daoPandora.mapper.detail;

import com.huatu.ztk.question.daoPandora.entity.QuestionAdvice;
import com.huatu.ztk.question.daoPandora.mapper.QuestionAdviceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author zhengyi
 * @date 2018/9/13 4:25 PM
 **/
@Component
public class QuestionAdviceDetail {
    private static final Logger logger = LoggerFactory.getLogger(QuestionAdviceDetail.class);
    @Autowired
    private QuestionAdviceMapper questionAdviceMapper;

    public void insert(QuestionAdvice questionAdviceNew) {
        questionAdviceNew.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        questionAdviceNew.setBizStatus(3);
        questionAdviceNew.setChecker(3);
        questionAdviceNew.setStatus(1);


        logger.info("测试问题反馈" + questionAdviceNew.toString());

        questionAdviceMapper.insert(questionAdviceNew);
    }
}