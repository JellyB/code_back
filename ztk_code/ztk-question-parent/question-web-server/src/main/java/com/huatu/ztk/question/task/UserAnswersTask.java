package com.huatu.ztk.question.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.UserAnswers;
import com.huatu.ztk.question.common.QuestionReidsKeys;
import com.huatu.ztk.question.service.QuestionRecordService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 试题用户答案处理，该任务用于计算试题的meta信息
 * Created by shaojieyue
 * Created time 2016-09-05 15:09
 */
public class UserAnswersTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(UserAnswersTask.class);

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private QuestionRecordService questionRecordService;

    @Override
    public void onMessage(Message message) {
        String content = new String(message.getBody());
        //logger.info("userAnswerTask receive message,data={}", content);
        UserAnswers userAnswers = null;
        try {
            userAnswers = JsonUtil.toObject(content, UserAnswers.class);
        }catch (Exception e){
            logger.error("ex",e);
            return;
        }

        //更新用户试题记录
        questionRecordService.updateQuestionRecord(userAnswers);

        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        for (Answer answer : userAnswers.getAnswers()) {
            //logger.info("统计信息 : 答题时间 = {},是否合法 = {}",answer.getTime(),(answer.getTime() < 5 || answer.getTime()>5*60));
            if (answer.getTime() < 5 || answer.getTime()>5*60) {//特殊情况不做处理
                continue;
            }

            //logger.info("统计信息 : 答案 = {},是否合法 = {}",answer.getAnswer(),(!StringUtils.isNumeric(answer.getAnswer()) || Integer.valueOf(answer.getAnswer()) < 1));

            //主观题的答案是string,暂时不处理
            if (!StringUtils.isNumeric(answer.getAnswer())
                    || Integer.valueOf(answer.getAnswer()) < 1) {//非法的答案
                continue;
            }

            //更新试题统计
            final String questionMetaKey = QuestionReidsKeys.getQuestionMetaKey(answer.getQuestionId());
            hashOperations.increment(questionMetaKey, "0", answer.getTime());//原子增加答题耗时
            hashOperations.increment(questionMetaKey, answer.getAnswer(), 1);//该答案选择数量+1
        }
    }
}
