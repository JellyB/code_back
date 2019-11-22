package com.huatu.sensors.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.sensors.constants.RabbitConstants;

@Configuration
@EnableApolloConfig(ApolloConfigConsts.NAMESPACE_TIKU_RABBIT)
public class RabbitConfig {
	/**
	 * 方法rabbitAdmin的功能描述:动态声明queue、exchange、routing
	 *
	 * @param connectionFactory
	 * @return
	 */
//	@Bean
//	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
//		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//		Queue deadQueue = new Queue(RabbitConstants.QUEUE_NAME_MATCH_ENROLL_ANALYTICS_QUEUE);
//		rabbitAdmin.declareQueue(deadQueue);
		// 声明死信队列
//		Queue deadQueue = new Queue(RabbitConstants.QUEUE_NAME_DEAD_QUEUE);
//		// 死信队列交换机
//		DirectExchange deadExchange = new DirectExchange(RabbitConstants.MQ_EXCHANGE_DEAD_QUEUE);
//		rabbitAdmin.declareQueue(deadQueue);
//		rabbitAdmin.declareExchange(deadExchange);
//		rabbitAdmin.declareBinding(BindingBuilder.bind(deadQueue).to(deadExchange).with(RabbitConstants.MQ_ROUTING_KEY_DEAD_QUEUE));
//
//		// 发放奖励队列交换机
//		DirectExchange exchange = new DirectExchange(RabbitConstants.MQ_EXCHANGE_SEND_AWARD);
//
//		// 声明发送优惠券的消息队列（Direct类型的exchange）
//		Queue couponQueue = queue(RabbitConstants.QUEUE_NAME_SEND_COUPON);
//		rabbitAdmin.declareQueue(couponQueue);
//		rabbitAdmin.declareExchange(exchange);
//		rabbitAdmin.declareBinding(
//				BindingBuilder.bind(couponQueue).to(exchange).with(RabbitConstants.MQ_ROUTING_KEY_SEND_COUPON));

		//return rabbitAdmin;
//	}

//	public Queue queue(String name) {
//		Map<String, Object> args = new HashMap<>();
//		// 设置死信队列
//		args.put("x-dead-letter-exchange", RabbitConstants.MQ_EXCHANGE_DEAD_QUEUE);
//		args.put("x-dead-letter-routing-key", RabbitConstants.MQ_ROUTING_KEY_DEAD_QUEUE);
//		// 设置消息的过期时间， 单位是毫秒
//		args.put("x-message-ttl", 30000);
//
//		// 是否持久化
//		boolean durable = true;
//		// 仅创建者可以使用的私有队列，断开后自动删除
//		boolean exclusive = false;
//		// 当所有消费客户端连接断开后，是否自动删除队列
//		boolean autoDelete = false;
//		return new Queue(name, durable, exclusive, autoDelete, args);
//	}
	
	
	@Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
	
	@Bean("rabbitFactory")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(60);
        factory.setConcurrentConsumers(20);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

}
