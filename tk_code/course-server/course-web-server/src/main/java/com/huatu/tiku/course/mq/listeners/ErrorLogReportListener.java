package com.huatu.tiku.course.mq.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dingtalk.chatbot.DingtalkChatbotClient;
import com.dingtalk.chatbot.message.TextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.common.bean.report.ExceptionReportMessage;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 上报错误消息处理
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class ErrorLogReportListener implements ChannelAwareMessageListener {

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${dingding.webhook:https://oapi.dingtalk.com/robot/send?access_token=338405433f6b9a54abf4d1e1681857c005a304a3ddbe19b38ae4ea869bd2adbd}")
	private String webhook;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		ExceptionReportMessage msg = objectMapper.readValue(message.getBody(), ExceptionReportMessage.class);
		if (msg != null) {
			log.error("error data report:{}", msg.toString());
			sendMsg("主机:" + msg.getHost() + "\r\n" + "应用名称:" + msg.getApplication() + "\r\n" + "方法名:" + msg.getMethod()
					+ "\r\n" + msg.getStacktrace());
		}

	}

	private void sendMsg(String content) {
		try {
			DingtalkChatbotClient client = new DingtalkChatbotClient();
			TextMessage msg = new TextMessage(content);
			msg.setIsAtAll(true);
			client.send(webhook, msg);
		} catch (Exception e) {
			log.error("dingding sendMsg error:{}", e);
		}
	}

}
