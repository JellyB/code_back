package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.TeacherScheduleReport;

/**
 * 教师课程统计Repository
 * 
 * @author Geek-S
 *
 */
public interface TeacherScheduleReportRepository extends BaseRepository<TeacherScheduleReport, Long> {

	/**
	 * 根据时间获取所有教师统计数据
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return 统计数据
	 */
	@Query(value = "SELECT CONCAT(r.day, '-', r.teacher_id), MAX(t.name), MAX(r.morning_time), MAX(r.afternoon_time), MAX(r.evening_time) FROM teacher_schedule_report r left join teacher t on r.teacher_id = t.id WHERE r.year = ?1 and r.month = ?2 GROUP BY r.teacher_id, r.day", nativeQuery = true)
	List<Object[]> getReports(Integer year, Integer month);

	/**
	 * 根据时间，教师IDs获取所有教师统计数据
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @param teacherIds
	 *            教师IDs
	 * @return 统计数据
	 */
	@Query(value = "SELECT CONCAT(r.day, '-', r.teacher_id), MAX(t.name), MAX(r.morning_time), MAX(r.afternoon_time), MAX(r.evening_time) FROM teacher_schedule_report r left join teacher t on r.teacher_id = t.id WHERE r.year = ?1 and r.month = ?2 and teacher_id in (?3) GROUP BY r.teacher_id, r.day", nativeQuery = true)
	List<Object[]> getReports(Integer year, Integer month, Long[] teacherIds);

	/**
	 * 获取上课记录
	 * 
	 * @param start
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @return 上课数据
	 */
	@Query(value = "select cl.date, clt.teacherId, cl.timeBegin, cl.timeEnd from CourseLiveTeacher clt join clt.courseLive cl join cl.course c where c.status = 5 and cl.sourceId is null and cl.date >= ?1 and cl.date < ?2")
	List<Object[]> findSchedules(Date start, Date end);

	/**
	 * 指定时间上课记录
	 * @param year
	 * @param month
	 * @return 直播id 日期 时间 教师id 教师名字
	 */
	@Query(value = "SELECT\n" +
			"\tcl.id,cl.date,cl.time_begin ,cl.time_end, clt.teacher_id,t.name\n" +
			"FROM\n" +
			"\tcourse_live_teacher clt\n" +
			"LEFT JOIN course_live cl ON cl.id = clt.course_live_id\n" +
			"LEFT JOIN course c ON c.id = cl.course_id\n" +
			"LEFT JOIN teacher t on t.id=clt.teacher_id\n" +
			"WHERE\n" +
			"date BETWEEN ?1 and ?2 and\n" +
			"\tclt.confirm = 1 and c.`status`=5 \n" +
			"ORDER BY cl.date ,time_begin,time_end", nativeQuery = true)
	List<Object[]> getReports2(String year, String month);

	@Query(value = "SELECT\n" +
			"\tcl.id,cl.date,cl.time_begin ,cl.time_end, clt.teacher_id,t.name\n" +
			"FROM\n" +
			"\tcourse_live_teacher clt\n" +
			"LEFT JOIN course_live cl ON cl.id = clt.course_live_id\n" +
			"LEFT JOIN course c ON c.id = cl.course_id\n" +
			"LEFT JOIN teacher t on t.id=clt.teacher_id\n" +
			"WHERE\n" +
			"date BETWEEN ?1 and ?2 and\n" +
			"\tclt.confirm = 1 and c.`status`=5 and clt.teacher_id in (?3)\n" +
			"ORDER BY cl.date ,time_begin,time_end", nativeQuery = true)
	List<Object[]> getReports2(String year, String month, Long[] teacherIds);
}
