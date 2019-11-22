package com.huatu.tiku.teacher.service.impl.question;

import com.huatu.tiku.teacher.dao.mongo.OldQuestionDao;
import com.huatu.tiku.teacher.service.question.OldQuestionService;
import com.huatu.ztk.question.bean.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by huangqp on 2018\6\25 0025.
 */
@Service
public class OldQuestionServiceImpl implements OldQuestionService{

    @Autowired
    OldQuestionDao oldQuestionDao;
    @Override
    public Question findQuestion(Integer questionId) {
        return oldQuestionDao.findById(questionId);
    }

    @Override
    public List<Question> findQuestions(List<Integer> ids) {
        return oldQuestionDao.findByIds(ids);
    }
}

