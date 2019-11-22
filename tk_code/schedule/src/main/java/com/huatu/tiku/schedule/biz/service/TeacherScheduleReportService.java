package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.TeacherScheduleReport;
import com.huatu.tiku.schedule.biz.vo.TeacherScheduleReportVo;

/**
 * 教师课程统计Service
 * 
 * @author Geek-S
 *
 */
public interface TeacherScheduleReportService extends BaseService<TeacherScheduleReport, Long> {

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
//	TeacherScheduleReportVo list(Integer year, Integer month, Long[] teacherIds);

	TeacherScheduleReportVo list2(Integer year, Integer month, Long[] teacherIds);

	/**
	 * 教师课程统计
	 */
	void generateReport();

}
