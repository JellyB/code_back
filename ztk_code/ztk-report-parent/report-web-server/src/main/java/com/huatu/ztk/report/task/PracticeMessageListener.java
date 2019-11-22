package com.huatu.ztk.report.task;

import com.google.common.primitives.Longs;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.question.bean.QuestionUserMeta;
import com.huatu.ztk.report.service.DayPracticeService;
import com.huatu.ztk.report.service.PracticeSummaryService;
import com.huatu.ztk.report.service.QuestionUserMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-05-30 20:44
 */
public class PracticeMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(PracticeMessageListener.class);

    @Autowired
    private DayPracticeService dayPracticeService;

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private PracticeSummaryService practiceSummaryService;

    @Autowired
    private QuestionUserMetaService questionUserMetaService;

    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        Map data = new HashMap();
        try {
            data = JsonUtil.toMap(text);
        }catch (Exception e){
            logger.error("ex",e);
        }
        Long id = null;
        if (data.containsKey("id")) {
            id = Longs.tryParse(data.get("id").toString());
        }
        if (id == null) {
            logger.error("message not contain key id,skip it. data={}",text);
            return;
        }

        AnswerCard answerCard = practiceCardDubboService.findById(id);
        if (answerCard == null) {
            return;
        }
        //添加练习次数
        dayPracticeService.addPractice(answerCard);
        //更新统计
        practiceSummaryService.updateSummary(answerCard);
        //用户数据统计
        questionUserMetaService.updateUserQuestionMeta(answerCard);
    }
}
