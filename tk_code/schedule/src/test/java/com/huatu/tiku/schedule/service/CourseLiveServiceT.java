package com.huatu.tiku.schedule.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.vo.CourseLiveRollingVo;
import com.huatu.tiku.schedule.biz.vo.CourseLiveScheduleVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CourseLiveServiceT extends ScheduleApplicationTests {

	@Autowired
	private CourseLiveService courseLiveService;

	@Test
	@Transactional
	public void scheduled() {
		List<ExamType> examTypes = Arrays.asList(ExamType.GWY);

		Long subjectId = 1L;

		Calendar now = Calendar.getInstance();

		Date dateEnd = now.getTime();

		now.set(Calendar.MONTH, 2);

		Date dateBegin = now.getTime();

		String courseName = "综合";

		Long liveRoomId = 1L;

		Pageable pageable = new PageRequest(0, 7);

		Page<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.schedule(examTypes, subjectId, dateBegin,
				dateEnd, courseName, courseName, liveRoomId, pageable);

		courseLiveScheduleVos.forEach(courseLiveScheduleVo -> {
			log.info(courseLiveScheduleVo.toString());
		});
	}

	@Test
	@Transactional
	public void findForRolling() {
		Long currentCourseId = 1L;

		Date date = Date.from(LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant());

		Long courseId = null;

		ExamType examType = ExamType.GWY;

		Long subjectId = null;

		List<CourseLiveRollingVo> courseLiveRollingVos = courseLiveService.findForRolling(currentCourseId,
				Arrays.asList(date), courseId, examType, subjectId);

		courseLiveRollingVos.forEach(courseLiveRollingVo -> {
			log.info("{}", courseLiveRollingVo);
		});
	}
}
