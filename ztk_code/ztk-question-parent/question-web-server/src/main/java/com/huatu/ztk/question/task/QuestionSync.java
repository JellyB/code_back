package com.huatu.ztk.question.task;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.common.QuestionCache;
import com.huatu.ztk.question.controller.InitController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 普通试题同步任务
 * Created by shaojieyue
 * Created time 2016-05-16 12:03
 */

@Component
public class QuestionSync implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSync.class);

    @Autowired
    private InitController initController;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public void onMessage(org.springframework.amqp.core.Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        try {
            final Map data = JsonUtil.toMap(text);
            final Integer questionId = Ints.tryParse(data.get("puKey").toString());
            if (questionId != null) {
                initController.syncQuestion(questionId,null);
                //移除缓存
                QuestionCache.remove(questionId);
            }else {
                logger.error("valid message. puKey not exist.");
            }
        }catch (Exception e){
            logger.error("ex",e);
            redisTemplate.opsForList().leftPush("sync-error-111",text);
        }
    }
}
