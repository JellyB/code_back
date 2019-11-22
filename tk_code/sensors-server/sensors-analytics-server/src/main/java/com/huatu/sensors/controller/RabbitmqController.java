package com.huatu.sensors.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.common.spring.web.MediaType;
import com.huatu.sensors.constants.RabbitConstants;
import com.huatu.sensors.mq.message.MockStartMessage;
import com.huatu.sensors.mq.sender.RabbitSender;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

@RestController
public class RabbitmqController {

	@Autowired
	private RabbitSender rabbitSender;

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	@PostMapping(value = "sendMsg", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object sendMsg(String name) {
		MockStartMessage sendMessage = new MockStartMessage();
		sendMessage.setId(1);
		sendMessage.setTerminal(999);
		rabbitSender.sendMessage("", RabbitConstants.QUEUE_NAME_MATCH_ENROLL_ANALYTICS_QUEUE, sendMessage);
		return sendMessage;
	}

	@GetMapping(value = "testSensors")
	public Object testSensors(String name) throws InvalidArgumentException {
		sensorsAnalytics.track("12345678", false, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), null);
		sensorsAnalytics.flush();
		return "ok";
	}
}
