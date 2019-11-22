package com.huatu.ztk.question.service;

import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.dao.QuestionExtendDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shaojieyue
 * Created time 2017-02-17 09:39
 */

@Service
public class QuestionExtendService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionExtendService.class);

    @Autowired
    private QuestionExtendDao questionExtendDao;

    public QuestionExtend findById(int qid) {
        return questionExtendDao.findById(qid);
    }
}
