package com.huatu.ztk.paper.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.MatchAnswers;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperRewardService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by linkang on 2017/10/25 下午6:23
 */
public class MatchAnswerCardSubmitTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MatchAnswerCardSubmitTask.class);


    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperRewardService paperRewardService;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        if (StringUtils.isBlank(text)){
            return;
        }

        try {
            MatchAnswers matchAnswers = JsonUtil.toObject(text, MatchAnswers.class);
            long practiceId = matchAnswers.getPracticeId();
            long userId = matchAnswers.getUserId();
            String uname = matchAnswers.getUname();
            //交卷
            AnswerCard answerCard = paperAnswerCardService.submitPractice(practiceId, userId,
                    matchAnswers.getAnswers(), matchAnswers.getArea(),TerminalType.ANDROID,"7.0.0");

            //送金币
            paperRewardService.sendSubmitPracticeMsg(userId, uname, answerCard);
        } catch (Exception e) {
            logger.error("submit error,{}",text);
            e.printStackTrace();
        }
    }
}
