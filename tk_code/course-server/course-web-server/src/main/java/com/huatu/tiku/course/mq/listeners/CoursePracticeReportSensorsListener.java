package com.huatu.tiku.course.mq.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.bean.vo.CoursePracticeReportSensorsVo;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.v6.SensorsService;

import lombok.extern.slf4j.Slf4j;

/**
 * 直播随堂练上报数据到神策监听
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class CoursePracticeReportSensorsListener {

	@Autowired
	private SensorsService sensorsService;

	@RabbitListener(queues = RabbitMqConstants.COURSE_PRACTICE_REPORT_SENSORS_QUEUE)
	public void onMessage(String message) {
		log.error("直播随堂练上报监听收到消息:{}", message);
		CoursePracticeReportSensorsVo payload = JSONObject.parseObject(message, CoursePracticeReportSensorsVo.class);
		log.error("直播随堂练上报监听解析消息:{}", payload.toString());
		sensorsService.reportCoursePracticeData(payload);
	}

}
