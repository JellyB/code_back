package com.arj.monitor.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ListAddressResolver;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RabbitMQCollectionService {

	@Value("${collect.rabbitmq.address}")
	private String hostStr;

	@Value("${collect.rabbitmq.username}")
	private String username;

	@Value("${collect.rabbitmq.password}")
	private String password;

	@Value("${collect.rabbitmq.queue}")
	private String queue;

	public String collect() {
		String ret = "";
		ConnectionFactory cf = null;
		Connection connection = null;
		try {
			cf = new ConnectionFactory();
			List<String> hostList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(hostStr);
			List<Address> addresslist = Lists.newArrayList();
			hostList.forEach(host -> {
				addresslist.add(new Address(host, 5672));
			});
			ListAddressResolver addressResolver = new ListAddressResolver(Lists.newArrayList(addresslist));
			cf.setUsername(username);
			cf.setPassword(password);
			connection = cf.newConnection(addressResolver);

			// 创建 channel实例
			Channel channel = connection.createChannel();

			channel.queueDeclare(queue, false, false, false, null);
			channel.basicPublish("", queue, null, SerializationUtils.serialize(new Date().getTime()));
			DefaultConsumer consumer = new DefaultConsumer(channel) {
				// 当消息到达时执行回调方法
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {
					Long message = (Long) SerializationUtils.deserialize(body);
					log.info("receive rabbit msg:" + message);
				}
			};
			// 监听队列
			channel.basicConsume(queue, true, consumer);
		} catch (IOException e) {
			ret = e.getMessage();
			log.error("rabbit check error:{}", e);
		} catch (TimeoutException e) {
			ret = e.getMessage();
			log.error("rabbit check error:{}", e);
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				log.error("MQ close err:{}",e.getMessage());
			}
		}

		return ret;
	}

	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost("192.168.100.21");
		cf.setPort(5672);
		cf.setUsername("rabbitmq_ztk");
		cf.setPassword("rabbitmq_ztk");
		Connection connection = cf.newConnection();
		// 创建 channel实例
		Channel channel = connection.createChannel();

		channel.queueDeclare("monitor_queue", false, false, false, null);
		channel.basicPublish("", "monitor_queue", null, SerializationUtils.serialize("111"));
		// template.convertAndSend("", "monitor_queue", "111");
		DefaultConsumer consumer = new DefaultConsumer(channel) {
			// 当消息到达时执行回调方法
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = (String) SerializationUtils.deserialize(body);
				System.out.println("[Receive]：" + message);
			}
		};
		// 监听队列
		channel.basicConsume("monitor_queue", true, consumer);

	}

}
