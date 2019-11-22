package com.huatu.tiku.course.mq.listeners;

import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 课后作业 mongo 答题卡刷入 mysql
 */
@Slf4j
//@Component
public class CourseWorkReportUsersListener {

	//@Autowired
	private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

	//@RabbitListener(queues = RabbitMqConstants.COURSE_WORK_REPORT_USERS_DEAL_QUEUE)
	public void onMessage(String message) {
		courseExercisesProcessLogManager.dealCourseWorkReportUsers(message);
	}
}
