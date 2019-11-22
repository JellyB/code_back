package com.huatu.ztk.question.task;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.controller.InitController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


/**
 * 复合题同步
 * Created by shaojieyue
 * Created time 2016-05-16 14:49
 */
public class MultiQuestionSync implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MultiQuestionSync.class);

    @Autowired
    private InitController initController;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        try {
            final Map data = JsonUtil.toMap(text);
            final Integer questionId = Ints.tryParse(data.get("puKey").toString());
            initController.syncMultiQuestion(questionId,null);
        }catch (Exception e){
            logger.error("ex",e);
        }
    }
}
