package com.huatu.tiku.schedule.repository;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.CourseLiveRepository;

public class CourseLiveRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private CourseLiveRepository courseLiveRepository;

	@Test
	public void delete() {
		Long id = 1L;

		courseLiveRepository.delete(id);
	}

	@Test
	public void findByCourseIdAndDateInAndTimeBeginAndTimeEnd() {
		Long courseId = 1L;

		List<Date> dates = Lists.newArrayList(new Date());

		Integer timeBegin = 800;

		Integer timeEnd = 1000;

		courseLiveRepository.findByCourseIdAndDateInAndTimeBeginAndTimeEnd(courseId, dates, timeBegin, timeEnd);
	}
}
