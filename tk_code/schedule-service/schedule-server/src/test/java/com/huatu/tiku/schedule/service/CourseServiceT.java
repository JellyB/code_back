package com.huatu.tiku.schedule.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.CourseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CourseServiceT extends ScheduleApplicationTests {

	@Autowired
	private CourseService courseService;

	@Test
	public void getCourseList() {
		ExamType examType = null;

		String name = "综合";

		Long id = null;

		Long subjectId = null;

		Calendar now = Calendar.getInstance();

		Date dateEnd = now.getTime();

		now.set(Calendar.MONTH, now.get(Calendar.MONTH) - 7);

		Date dateBegin = now.getTime();

		String teacherName = "测试";

		CourseStatus status = CourseStatus.JSAP;

		Pageable page = new PageRequest(0, 7);

		Page<Course> courses = courseService.getCourseListZJ(examType, name, id, subjectId, dateBegin, dateEnd,
				teacherName, status, page);
		courses.forEach(course -> {
			log.info("Course name is {}", course.getName());
		});
	}

	@Test
	public void submitCourseLive() {
		Long id = 1L;

		Map<String, Object> result = courseService.submitCourseLive(id);

		log.info("{}", result);
	}

	@Test
	public void sendCourseLiveTeacherConfirmSms() {
		Long id = 49L;

		courseService.sendCourseLiveTeacherConfirmSms(id);
	}
}
