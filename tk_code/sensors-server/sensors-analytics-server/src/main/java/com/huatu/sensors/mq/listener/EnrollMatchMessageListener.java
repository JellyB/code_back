package com.huatu.sensors.mq.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.huatu.sensors.constants.RabbitConstants;
import com.huatu.sensors.mq.message.EnrollMatchMessage;
import com.huatu.sensors.service.SensorsService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 模考大赛确认报名队列监听
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Service
public class EnrollMatchMessageListener {

	@Autowired
	private SensorsService sensorsService;

	//@RabbitListener(queues = RabbitConstants.QUEUE_NAME_MATCH_ENROLL_ANALYTICS_QUEUE)
	public void process(Channel channel, Message message) throws Exception {
		try {
			// channel.basicQos(0, 1, false);
			String payLoad = new String(message.getBody());
			EnrollMatchMessage msg = JSON.parseObject(payLoad, EnrollMatchMessage.class);
			log.info("[{}]处理队列接收数据，消息体：{}", RabbitConstants.QUEUE_NAME_MATCH_ENROLL_ANALYTICS_QUEUE,
					JSON.toJSONString(msg));
			sensorsService.enrollMatchAnalytics(msg);
			// 确认消息已经消费成功
			// channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

		} catch (Exception e) {
			log.error("mock开始答题处理消息处理失败:{}", e);
		}
	}
}
