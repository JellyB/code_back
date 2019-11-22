package com.huatu.sensors.service;

import com.huatu.sensors.mq.message.EnrollMatchMessage;
import com.huatu.sensors.mq.message.MockSubmitMessage;

/**
 * 神策埋点业务处理
 * 
 * @author zhangchong
 *
 */
public interface SensorsService {

	/**
	 * 模考开始答题埋点处理
	 * @param token
	 * @param id
	 * @param subject
	 * @param terminal
	 */
	void createMatchAnswerCardAnalytics(String token, int id, int subject, int terminal);
	/**
	 * 模考交卷埋点处理
	 * @param msg
	 */
	void submitMatchAnswerCardAnalytics(MockSubmitMessage msg);
	
	/**
	 * 模考大赛报名
	 * @param msg
	 */
	void enrollMatchAnalytics(EnrollMatchMessage msg);
	
	
}
