package com.huatu.tiku.course.mq.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.v6.PeriodTestServiceV6;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * 阶段测试交卷监听
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class PeriodAnswerCardSubmitListener {

	@Autowired
	private PeriodTestServiceV6 periodTestServiceV6;

	@RabbitListener(queues = RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO)
	public void onMessage(String message) {

		log.info("periodAnswerCardSubmit recive msg:{}", message);
		PeriodTestSubmitlPayload payload = JSONObject.parseObject(message, PeriodTestSubmitlPayload.class);
		periodTestServiceV6.uploadPeriodStatus2PHP(payload);
	}
	
}
