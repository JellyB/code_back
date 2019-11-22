package com.huatu.tiku.course.mq.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.v1.practice.CoursewarePracticeQuestionInfoService;
import com.huatu.tiku.course.service.v6.PeriodTestServiceV6;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * 直播随堂练数据持久化监听
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class CoursePracticeSave2DbListener {

	@Autowired
	private CoursewarePracticeQuestionInfoService coursewarePracticeQuestionInfoService;

	@RabbitListener(queues = RabbitMqConstants.COURSE_PRACTICE_SAVE_DB_QUEUE)
	public void onMessage(String message) {

		log.info("CoursePracticeSave2DbListener recive msg:{}", message);
		coursewarePracticeQuestionInfoService.generateCoursewareAnswerCardInfo(Long.parseLong(message));
	}

}
