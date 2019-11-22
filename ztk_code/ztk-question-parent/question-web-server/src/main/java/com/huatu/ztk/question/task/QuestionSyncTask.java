package com.huatu.ztk.question.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionRabbitMqKeys;
import com.huatu.ztk.question.dao.QuestionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 更新试题缓存
 * Created by huangqp on 2018\5\3 0003.
 */
@Component
public class QuestionSyncTask implements MessageListener {
    private final static Logger logger = LoggerFactory.getLogger(QuestionSyncTask.class);
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public void onMessage(Message message) {
        logger.info(">>>>> get questionSyncTaskInfo message = {} ",message);
        String content = new String(message.getBody());
        int id = 0;
        try {
            Map map = JsonUtil.toObject(content, Map.class);
            if (map != null) {
                id = Integer.parseInt(String.valueOf(map.get("id")));
            }
        } catch (Exception e) {
            logger.error("proccess fail. message={}", message);
            e.printStackTrace();
            return;
        }

        if (id == 0) {
            logger.error("proccess fail. message={}", message);
            return;
        }
        try {
            Thread.sleep(100);
            Question question = questionDao.findById(id);
            logger.info("rabbit's question={}", question);
            if (question != null) {
                //试题刷新（附带刷新缓存）
                questionDubboService.update(question);
            }else{
                Map<String, Integer> data = new HashMap<>();
                data.put("qid", question.getId());
                rabbitTemplate.convertAndSend(QuestionRabbitMqKeys.QUESTION_UPDATE_EXCHANGE, "", data);
            }
        } catch (Exception e) {
            logger.error("process fail. message={}，error={}", message, e.getMessage());
           // e.printStackTrace();
        }
    }
}
