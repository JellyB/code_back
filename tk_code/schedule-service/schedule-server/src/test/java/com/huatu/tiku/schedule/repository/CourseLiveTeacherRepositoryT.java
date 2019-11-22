package com.huatu.tiku.schedule.repository;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.CourseLiveTeacherRepository;

public class CourseLiveTeacherRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private CourseLiveTeacherRepository courseLiveTeacherRepository;

	@Test
	public void bindTeacher() {
		Long courseLiveTeacherId = 1L;

		Long teacherId = 1L;

		courseLiveTeacherRepository.bindTeacher(courseLiveTeacherId, teacherId, CourseConfirmStatus.DQR.ordinal());
	}
}
