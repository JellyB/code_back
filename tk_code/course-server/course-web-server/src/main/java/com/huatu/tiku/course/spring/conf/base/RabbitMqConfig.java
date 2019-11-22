package com.huatu.tiku.course.spring.conf.base;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_RABBIT;
import static com.huatu.tiku.common.consts.RabbitConsts.ARG_DLK;
import static com.huatu.tiku.common.consts.RabbitConsts.ARG_DLX;
import static com.huatu.tiku.common.consts.RabbitConsts.DLK_DEFAULT;
import static com.huatu.tiku.common.consts.RabbitConsts.DLX_DEFAULT;
import static com.huatu.tiku.common.consts.RabbitConsts.QUEUE_REWARD_ACTION;
import static com.huatu.tiku.common.consts.RabbitConsts.QUEUE_SEND_FREE_COURSE;
import static com.huatu.tiku.common.consts.RabbitConsts.QUEUE_USER_NICK_UPDATE;

import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.mq.listeners.ErrorLogReportListener;
import com.huatu.tiku.course.mq.listeners.RewardMessageListener;

/**
 * @author hanchao
 * @date 2017/9/4 14:05
 */
@EnableApolloConfig(NAMESPACE_TIKU_RABBIT)
@Configuration
public class RabbitMqConfig {
	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter(@Autowired ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	/**
	 * queue声明
	 *
	 * @return
	 */
	@Bean
	public Queue sendFreeCourseQueue() {
		return new Queue(QUEUE_SEND_FREE_COURSE);
	}

	@Bean
	public Queue userNickUpdateQueue() {
		return new Queue(QUEUE_USER_NICK_UPDATE);
	}

	@Bean
	public Queue rewardActionQueue() {
		Map<String, Object> arguments = Maps.newHashMap();
		arguments.put(ARG_DLX, DLX_DEFAULT);
		arguments.put(ARG_DLK, DLK_DEFAULT);
		Queue rewardActionQueue = new Queue(QUEUE_REWARD_ACTION, true, false, false, arguments);
		return rewardActionQueue;
	}

	@Bean
	public SimpleMessageListenerContainer rewardMessageListenerContainer(@Autowired ConnectionFactory connectionFactory,
			@Autowired(required = false) @Qualifier("coreThreadPool") ThreadPoolTaskExecutor threadPoolTaskExecutor,
			@Autowired RewardMessageListener rewardMessageListener, @Autowired AmqpAdmin amqpAdmin) {
		SimpleMessageListenerContainer manualRabbitContainer = new SimpleMessageListenerContainer();
		manualRabbitContainer.setQueueNames(QUEUE_REWARD_ACTION);
		manualRabbitContainer.setConnectionFactory(connectionFactory);
		if (amqpAdmin instanceof RabbitAdmin) {
			manualRabbitContainer.setRabbitAdmin((RabbitAdmin) amqpAdmin);
		}
		if (threadPoolTaskExecutor != null) {
			manualRabbitContainer.setTaskExecutor(threadPoolTaskExecutor);
		}
		manualRabbitContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		manualRabbitContainer.setConcurrentConsumers(threadPoolTaskExecutor.getCorePoolSize() / 4);
		manualRabbitContainer.setMaxConcurrentConsumers(threadPoolTaskExecutor.getCorePoolSize() / 4);
		manualRabbitContainer.setMessageListener(rewardMessageListener);
		return manualRabbitContainer;
	}

	@Bean
	public Queue courseWorkSubmitCardInfo() {
		return new Queue(RabbitMqConstants.COURSE_WORK_SUBMIT_CARD_INFO);
	}

	/**
	 * 直播回放处理queue
	 * 
	 * @return
	 */
	@Bean
	public Queue playBackDealInfo() {
		return new Queue(RabbitMqConstants.PLAY_BACK_DEAL_INFO);
	}

	/**
	 * 阶段测试提交答题卡队列
	 * 
	 * @return
	 */
	@Bean
	public Queue periodTestSubmitCardInfo() {
		return new Queue(RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO);
	}

	/**
	 * 直播上报队列
	 * 
	 * @return
	 */
	@Bean
	public Queue courseLiveReportLive() {
		return new Queue(RabbitMqConstants.COURSE_LIVE_REPORT_LOG);
	}

	/**
	 * 课后作业数据修复队列
	 * 
	 * @return
	 */
	@Bean
	public Queue courseWorkCorrect() {
		return new Queue(RabbitMqConstants.COURSE_EXERCISES_PROCESS_LOG_CORRECT_QUEUE);
	}

	/**
	 * 直播随堂练信息持久化
	 * 
	 * @return
	 */
	@Bean
	public Queue coursePractice2Db() {
		return new Queue(RabbitMqConstants.COURSE_PRACTICE_SAVE_DB_QUEUE);
	}

	/**
	 * 录播随堂练信息持久化
	 * 
	 * @return
	 */
	@Bean
	public Queue CourseBreakPointPracticeSave2Db() {
		return new Queue(RabbitMqConstants.COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE);
	}

	/**
	 * 直播随堂练上报到神策
	 * 
	 * @return
	 */
	@Bean
	public Queue CoursePracticeReportSensors() {
		return new Queue(RabbitMqConstants.COURSE_PRACTICE_REPORT_SENSORS_QUEUE);
	}


	/**
	 * 课后作业 mongo -> mysql 处理队列
	 * @return
	 */
	//@Bean
	public Queue CourseWorkReportDealQueue(){
		return new Queue(RabbitMqConstants.COURSE_WORK_REPORT_USERS_DEAL_QUEUE);
	}
	
	@Bean
	public SimpleMessageListenerContainer reportLogListenerContainer(@Autowired ConnectionFactory connectionFactory,
			@Autowired(required = false) @Qualifier("coreThreadPool") ThreadPoolTaskExecutor threadPoolTaskExecutor,
			@Autowired ErrorLogReportListener reportListener, @Autowired AmqpAdmin amqpAdmin) {
		SimpleMessageListenerContainer manualRabbitContainer = new SimpleMessageListenerContainer();
		manualRabbitContainer.setQueueNames(RabbitConsts.QUEUE_REPORT);
		manualRabbitContainer.setConnectionFactory(connectionFactory);
		if (amqpAdmin instanceof RabbitAdmin) {
			manualRabbitContainer.setRabbitAdmin((RabbitAdmin) amqpAdmin);
		}
		if (threadPoolTaskExecutor != null) {
			manualRabbitContainer.setTaskExecutor(threadPoolTaskExecutor);
		}
		manualRabbitContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
		manualRabbitContainer.setConcurrentConsumers(threadPoolTaskExecutor.getCorePoolSize() / 4);
		manualRabbitContainer.setMaxConcurrentConsumers(threadPoolTaskExecutor.getCorePoolSize() / 4);
		manualRabbitContainer.setMessageListener(reportListener);
		return manualRabbitContainer;
	}
	
}
