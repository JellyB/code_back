package com.huatu.tiku.schedule.biz.service;

import java.util.List;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.dto.SaveCourseLiveTeacherBatchDto;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;

/**
 * 课程直播教师Service
 * 
 * @author Geek-S
 *
 */
public interface CourseLiveTeacherService extends BaseService<CourseLiveTeacher, Long> {

	/**
	 * 绑定课程直播的教师
	 * 
	 * @param courseLiveTeacherId
	 *            课程直播明细ID
	 * @param teacherId
	 *            教师ID
	 * @return 操作影响数
	 */
	int bindTeacher(Long courseLiveTeacherId, Long teacherId);

	/**
	 * 批量添加课程直播教师
	 * 
	 * @param courseLiveTeacherBatchDto
	 *            课程直播教师
	 * @return 课程直播教师
	 */
	List<CourseLiveTeacher> saveCourseLiveTeacherBatch(SaveCourseLiveTeacherBatchDto courseLiveTeacherBatchDto,boolean isCover);

	/**
	 * 修改教师任务状态
	 * @param teacherId 教师集合
	 * @param ids id集合
	 * @param courseConfirmStatus 确认状态
	 */
    int updateTaskTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

	/**
	 * 修改阶段
	 * @param courseLiveTeacherId 直播教师id
	 * @param coursePhase 阶段
	 * @return
	 */
    int savaCoursePhase(Long courseLiveTeacherId, CoursePhase coursePhase);

	/**
	 * 修改模块
	 * @param courseLiveTeacherId 直播教师id
	 * @param moduleId 模块
	 * @return
	 */
	int savaModule(Long courseLiveTeacherId, Long moduleId);

	/**
	 * 修改授课科目
	 * @param courseLiveTeacherId 直播教师id
	 * @param subjectId 科目
	 * @return
	 */
	int savaSubject(Long courseLiveTeacherId, Long subjectId);

	/**
	 * 修改授课级别
	 * @param courseLiveTeacherId 直播教师id
	 * @param teacherCourseLevel 级别
	 * @return
	 */
	int savaLiveTeacherLevel(Long courseLiveTeacherId, TeacherCourseLevel teacherCourseLevel);

	/**
	 * 根据课程直播教师id查询状态 如果为直播安排 抛出异常
	 * @param courseLiveTeacherId
	 */
    void findCourseStatusByCourseLiveTeacherId(Long courseLiveTeacherId);

	/**
	 * 根据直播ID和教师ID查询记录
	 * 
	 * @param courseLiveId
	 *            直播ID
	 * @param teacherId
	 *            教师ID
	 * @return 记录
	 */
	CourseLiveTeacher findByCourseLiveIdAndTeacherId(Long courseLiveId, Long teacherId);
}
