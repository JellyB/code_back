package main.java.com.huatu.ztk;

import com.huatu.ztk.BaseTest;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionRabbitMqKeys;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.sun.media.sound.SoftTuning;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-07-12 09:59
 */
public class QuestionUpdateTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionUpdateTest.class);


    @Autowired
    private QuestionDubboService questionDubboService;

    @Test
    public void aaTest(){
        final Question question = questionDubboService.findById(205533);
        System.out.println(JsonUtil.toJson(question));
    }

    @Test
    public void update(){
        final Question question = questionDubboService.findById(21936242);
        try {
            questionDubboService.update(question);
        } catch (IllegalQuestionException e) {
            e.printStackTrace();
        }
        System.out.println(JsonUtil.toJson(question));
    }
}
