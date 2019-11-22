package com.huatu.sensors.mq.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.huatu.sensors.constants.RabbitConstants;
import com.huatu.sensors.mq.message.MockStartMessage;
import com.huatu.sensors.service.SensorsService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * mock提交答案延迟队列消费
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Service
public class CreateMatchAnswerDeadMessageListener {

	@Autowired
	private SensorsService sensorsService;

	//@RabbitListener(queues = RabbitConstants.QUEUE_NAME_CREATE_MATCH_ANSWER_DEAD_QUEUE)
	public void process(Channel channel, Message message) throws Exception {
		try {
			//channel.basicQos(0, 1, false);
			String payLoad = new String(message.getBody());
			MockStartMessage msg = JSON.parseObject(payLoad, MockStartMessage.class);
			log.info("[{}]处理延迟队列消息队列接收数据，消息体：{}", RabbitConstants.QUEUE_NAME_CREATE_MATCH_ANSWER_DEAD_QUEUE,
					JSON.toJSONString(msg));
			sensorsService.createMatchAnswerCardAnalytics(msg.getToken(), msg.getId(), msg.getSubject(),
					msg.getTerminal());
			// 确认消息已经消费成功
			// channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

		} catch (Exception e) {
			log.error("mock开始答题处理消息处理失败:{}", e);
		}
	}
}
