package main.java.com.huatu.ztk;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionRabbitMqKeys;
import com.huatu.ztk.question.service.QuestionService;
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
public class QuestionServiceTest extends com.huatu.ztk.BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceTest.class);


    @Autowired
    private QuestionService questionService;

    @Test
    public void aaTest(){

        final Question question = questionService.findById(205533,null);
        System.out.println(JsonUtil.toJson(question));
    }

}
