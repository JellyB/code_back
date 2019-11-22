package interview.service;


import com.huatu.ztk.commons.JsonUtil;
import interview.bean.InterviewCorrectObject;
import interview.bean.InterviewScoreDescription;
import interview.util.CorrectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  13:52 .
 */
@Service
public class CorrectServiceV1 {
    private static final Logger logger = LoggerFactory.getLogger(CorrectServiceV1.class);

    @Autowired
    private CorrectUtil correctUtil;

    public Object correct(String answer) throws Exception {
        logger.info("传输进来的内容={}",answer);
        InterviewCorrectObject answerObject = JsonUtil.toObject(answer,InterviewCorrectObject.class);
        String userAnswer = answerObject.getAnswerContent();
        long answerCardId = answerObject.getQuestionRecordId();
        List<InterviewScoreDescription> scoreDescs = answerObject.getScoreDescs();
        int questionRecordType = answerObject.getQuestionRecordType();
        logger.info("userAnswer={},answerCardId={},scoreDescs={}",userAnswer,answerCardId,scoreDescs);
        return correctUtil.correctInterview(userAnswer,answerCardId,scoreDescs,questionRecordType);
    }
}
