package com.huatu.tiku.course.mq.listeners;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.bean.CourseBreakPointPracticeDto;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.v1.CourseBreakpointQuestionService;

import lombok.extern.slf4j.Slf4j;

/**
 * 录播随堂练数据持久化监听
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class CourseBreakPointPracticeSave2DbListener {
	
	@Autowired
	private CourseBreakpointQuestionService courseBreakpointQuestionService;

	@RabbitListener(queues=RabbitMqConstants.COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE)
	public void onMessage(String message) {
		log.error("录播随堂练监听收到消息:{}", message);
		List<CourseBreakPointPracticeDto> payloadList = JSONObject.parseArray(message, CourseBreakPointPracticeDto.class);
		log.error("录播随堂练list:{}", payloadList);
		courseBreakpointQuestionService.saveCourseBreakpointPracticeInfo(payloadList);
	}

}
