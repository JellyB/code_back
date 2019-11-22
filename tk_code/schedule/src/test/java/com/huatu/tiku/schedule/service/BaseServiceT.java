package com.huatu.tiku.schedule.service;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;

public class BaseServiceT extends ScheduleApplicationTests {

	@Autowired
	private CourseLiveTeacherService courseLiveTeacherService;

	@Test
	public void delete() {
		List<Long> ids = Lists.newArrayList(6L, 7L);

		courseLiveTeacherService.delete(ids);
	}

}
