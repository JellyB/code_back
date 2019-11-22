package com.huatu.ztk.backend.mq;

import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 修改试题信息
 * Created by huangqp on 2018\5\3 0003.
 */
public class QuestionSyncTask implements MessageListener {
    private final static Logger logger = LoggerFactory.getLogger(QuestionSyncTask.class);
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionDao questionDao;
    @Override
    public void onMessage(Message message) {
        String content = new String(message.getBody());
        int id = 0;
        try {
            Map map = JsonUtil.toObject(content, Map.class);
            if(map!=null){
                id = Integer.parseInt(String.valueOf(map.get("id")));
            }
        } catch (Exception e) {
            logger.error("proccess fail. message={}", message, e);
            return;
        }

        if(id==0){
            logger.error("proccess fail. message={}", message);
            return;
        }
        try {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Question question = questionDao.findQuestionById(id);
            logger.info("rabbit's question={}",question);
            if(question!=null){
                questionDubboService.update(question);
            }
        } catch (IllegalQuestionException e) {
            logger.error("process fail. message={}，error={}", message, e);
            e.printStackTrace();
        }
    }
}
