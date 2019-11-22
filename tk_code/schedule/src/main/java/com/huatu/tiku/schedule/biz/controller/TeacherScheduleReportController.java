package com.huatu.tiku.schedule.biz.controller;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.biz.service.TeacherScheduleReportService;
import com.huatu.tiku.schedule.biz.vo.TeacherScheduleReportVo;

/**
 * 教师课程统计Controller
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("teacherScheduleReport")
public class TeacherScheduleReportController {

	private final TeacherScheduleReportService teacherScheduleReportService;

	@Autowired
	public TeacherScheduleReportController(TeacherScheduleReportService teacherScheduleReportService) {
		this.teacherScheduleReportService = teacherScheduleReportService;
	}

	/**
	 * 根据条件查询教师课程统计
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @param teacherIds
	 *            教师IDs
	 * @return 教师课程统计
	 */
	@GetMapping
	public TeacherScheduleReportVo list(Integer year, Integer month, Long[] teacherIds) {
		// 校验参数是否合法
		Calendar now = null;
		if (year == null) {
			now = Calendar.getInstance();
			year = now.get(Calendar.YEAR);
		}

		if (month == null) {
			if (now == null) {
				now = Calendar.getInstance();
			}
			month = now.get(Calendar.MONTH) + 1;
		}

		return teacherScheduleReportService.list2(year, month, teacherIds);
	}

}
