package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.essay.service.EssayFeedbackService;
import com.huatu.tiku.push.constant.RabbitMqKey;
import com.huatu.tiku.push.constant.SuggestFeedbackInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EssayFeedbackServiceImpl implements EssayFeedbackService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public Object pushFeedbackReply(SuggestFeedbackInfo info) {
        String message = JSONObject.toJSONString(info);
        rabbitTemplate.convertAndSend(RabbitMqKey.NOTICE_FEEDBACK_SUGGEST, message);
        return "意见反馈回复成功";
    }
}
