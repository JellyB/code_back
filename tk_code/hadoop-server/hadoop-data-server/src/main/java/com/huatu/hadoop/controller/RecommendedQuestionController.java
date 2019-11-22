package com.huatu.hadoop.controller;

import com.huatu.hadoop.bean.ZtkAnswerCardCTO;
import com.huatu.hadoop.service.RecommendedQuestionService;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController()
public class RecommendedQuestionController {

    @Autowired
    private RecommendedQuestionService questionService;


    @Autowired
    private Producer<String, String> producer;

   @PostMapping("/v1/answer/card")
    public Object questionUserCache(
            @Token(check = false) UserSession userSession,
            @RequestBody ZtkAnswerCardCTO quc) throws Exception {

        int userId = userSession.getId();
        quc.setUserId(userId);

        producer.send(new KeyedMessage("question-record", quc.toString()));
        return true;
    }

    @PostMapping("/v1/answer/card/test")
    public Object questionUserCache(
            @RequestBody ZtkAnswerCardCTO quc) {

        producer.send(new KeyedMessage("question-record", quc.toString()));
        return true;
    }

}
