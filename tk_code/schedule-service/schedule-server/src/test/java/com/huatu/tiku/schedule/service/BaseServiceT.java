package com.huatu.tiku.schedule.service;

import java.util.Date;
import java.util.List;

import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;

public class BaseServiceT extends ScheduleApplicationTests {

	@Autowired
	private CourseLiveTeacherService courseLiveTeacherService;
	@Autowired
	private CourseLiveService courseLiveService;

	@Test
	public void test() {
		Boolean aBoolean = courseLiveService.timeCheck("1345", "1645", 1l, new Date(), null);
		System.out.println(aBoolean);
	}
	@Test
	public void delete() {
		List<Long> ids = Lists.newArrayList(6L, 7L);

		courseLiveTeacherService.delete(ids);
	}

}
