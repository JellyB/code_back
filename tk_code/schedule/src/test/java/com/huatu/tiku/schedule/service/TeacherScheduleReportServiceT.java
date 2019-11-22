package com.huatu.tiku.schedule.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.service.TeacherScheduleReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherScheduleReportServiceT extends ScheduleApplicationTests {

	@Autowired
	private TeacherScheduleReportService teacherScheduleReportService;

	@Test
	public void generateReport() {
		log.info("Mission start . . .");

		teacherScheduleReportService.generateReport();

		log.info("Mission complete . . .");
	}
}
