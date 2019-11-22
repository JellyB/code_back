package com.huatu.ztk.question.task;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 * 试题更新task
 * Created by shaojieyue
 * Created time 2016-07-12 09:49
 */

@Component
public class QuestionUpdateTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(QuestionUpdateTask.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        try {
            final Map data = JsonUtil.toMap(text);
            final Integer questionId = Ints.tryParse(data.get("qid").toString());

            //移除缓存，保证下次取到的是最新数据
            QuestionCache.remove(questionId);

            final Question question = questionDubboService.findById(questionId);
            if (question == null) {//不存在跳过
                logger.error("qid={} question not exist.",question);
                return;
            }
            if (question instanceof CompositeQuestion) {
                //如果是复合题，则删除其对应的子题
                CompositeQuestion compositeQuestion = (CompositeQuestion)question;
                final List<Integer> questions = compositeQuestion.getQuestions();
                for (Integer subQid : questions) {
                    QuestionCache.remove(subQid);
                }
            }
        }catch (Exception e){
            logger.error("ex，data={}",text,e);
        }
    }
}
