//package com.huatu.tiku.schedule.biz.task;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.huatu.tiku.schedule.biz.service.TeacherScheduleReportService;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 课程统计任务
// *
// * @author Geek-S
// *
// */
//@Component
//@Slf4j
//public class TeacherScheduleReportTask {
//
//	private final TeacherScheduleReportService teacherScheduleReportService;
//
//	@Autowired
//	public TeacherScheduleReportTask(TeacherScheduleReportService teacherScheduleReportService) {
//		this.teacherScheduleReportService = teacherScheduleReportService;
//	}
//
//	/**
//	 * 每天凌晨一点统计前一天数据
//	 */
//	@Scheduled(cron = "0 0 1 * * ?")
//	public void generateReport() {
//		log.info("统计数据开始 . . .");
//
//		teacherScheduleReportService.generateReport();
//
//		log.info("统计数据完成 . . .");
//	}
//}
