package com.huatu.tiku.schedule.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import com.huatu.tiku.schedule.biz.service.TeacherService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherServiceT extends ScheduleApplicationTests {

	@Autowired
	private TeacherService teacherService;

	@Test
	public void getAvailableTeachers() {
		Date date = Date.from(LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant());

		date = new Date();

		date = DateUtils.addDays(date, -2);

		Integer timeBegin = 800;

		Integer timeEnd = 1000;

		ExamType examType = ExamType.GWY;

		Long subjectId = null;

		Long courseId = null;

		TeacherCourseLevel teacherCourseLevel = TeacherCourseLevel.COMMON;

		List<TeacherScoreBean> teacherScoreBeans = teacherService.getAvailableTeachers(date, timeBegin, timeEnd,
				examType, subjectId, teacherCourseLevel, courseId);

		teacherScoreBeans.forEach(teacherScoreBean -> {
			log.info("Teacher name is {}", teacherScoreBean.getName());
		});
	}

	@Test
	public void updateRolesById() {
		Long id = 1L;

		List<Long> roleIds = Lists.newArrayList(1L, 3L);

		teacherService.updateRolesById(id, roleIds);
	}
}
