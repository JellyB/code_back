package com.huatu.ztk.question.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.QuestionAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by linkang on 7/22/16.
 */

@Repository
public class QuestionAdviceDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionAdviceDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveAdvice(QuestionAdvice questionAdvice) {
        logger.info("insert user advice: data={}", JsonUtil.toJson(questionAdvice));

        String sql = "insert v_question_correction_log(question_id,question_type,bl_sub_exam,error_type," +
                "BB105,EB102,error_descrp,BB103,qtype,question_area,module_id,subject) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                questionAdvice.getQid(),
                "o",                       //试题类型，客观题
                questionAdvice.getCatgory(),//科目
                questionAdvice.getErrorType(),//错误类型，4:其它错误
                questionAdvice.getUid(),//创建人
                questionAdvice.getContacts(),//联系方式
                questionAdvice.getContent(),//纠错内容
                System.currentTimeMillis()/1000,//创建时间
                questionAdvice.getQtype(),
                questionAdvice.getQuestionArea(),
                questionAdvice.getModuleId(),
                questionAdvice.getSubject()
        };
        jdbcTemplate.update(sql, params);
    }

}
