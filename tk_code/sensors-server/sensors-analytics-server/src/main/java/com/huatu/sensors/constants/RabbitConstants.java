package com.huatu.sensors.constants;

public class RabbitConstants {

	/**
	 * 死性队列EXCHANGE名称
	 */
	//public static final String MQ_EXCHANGE_DEAD_QUEUE = "test-dead-queue-exchange";

	/**
	 * mock开始答题死性队列名称
	 */
	public static final String QUEUE_NAME_CREATE_MATCH_ANSWER_DEAD_QUEUE = "create_match_answer_card_analytics_dead_queue";
	
	/**
	 * mock提交答案死性队列名称
	 */
	public static final String QUEUE_NAME_SUBMIT_MATCH_ANSWER_DEAD_QUEUE = "submit_match_answer_card_analytics_dead_queue";
	
	/**
	 * 模考确认报名队列名称
	 */
	public static final String QUEUE_NAME_MATCH_ENROLL_ANALYTICS_QUEUE = "match_enroll_analytics_queue";
	
	

	/**
	 * 死性队列路由名称
	 */
	//public static final String MQ_ROUTING_KEY_DEAD_QUEUE = "test-routing-key-dead-queue";

	/**
	 * 神策EXCHANGE名称
	 */
	//public static final String MQ_EXCHANGE_SEND_AWARD = "test-sensors-exchange";

	/**
	 * 神策模考队列名称
	 */
	//public static final String QUEUE_NAME_SEND_COUPON = "test-sensors-mock-queue";

	/**
	 * 神策模考路由key
	 */
	//public static final String MQ_ROUTING_KEY_SEND_COUPON = "test-routing-key-sensors-mock";
}
