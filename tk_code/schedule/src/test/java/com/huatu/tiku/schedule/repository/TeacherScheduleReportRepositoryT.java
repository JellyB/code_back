package com.huatu.tiku.schedule.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.TeacherScheduleReportRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherScheduleReportRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private TeacherScheduleReportRepository teacherScheduleReportRepository;

	@Test
	public void getReports() {
		Integer year = 2018;

		Integer month = 4;

		List<Object[]> result = teacherScheduleReportRepository.getReports(year, month);

		result.forEach(temp -> {
			log.info("{}", Arrays.toString(temp));
		});
	}

	@Test
	public void findSchedules() {
		ZonedDateTime zonedDateTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault());

		Date end = Date.from(zonedDateTime.toInstant());

		Date start = Date.from(zonedDateTime.minusDays(1).toInstant());

		List<Object[]> result = teacherScheduleReportRepository.findSchedules(start, end);

		result.forEach(temp -> {
			log.info(Arrays.toString(temp));
		});
	}
}
