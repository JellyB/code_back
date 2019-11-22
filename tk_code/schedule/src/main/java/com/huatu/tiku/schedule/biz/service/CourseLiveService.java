package com.huatu.tiku.schedule.biz.service;

import java.util.Date;
import java.util.List;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.vo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 课程直播Service
 * 
 * @author Geek-S
 *
 */
public interface CourseLiveService extends BaseService<CourseLive, Long> {

	/**
	 * 创建课程直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期集合
	 * @param times
	 *            时间集合
	 * @return 课程直播列表
	 */
	List<CourseLive> createCourseLive(Long courseId,Long subjectId, List<Date> dates, List<List<String>> times,Boolean token);

	/**
	 * 判断课程直播是否已经存在
	 * 
	 * @param courseId
	 *            课程ID
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return true/false
	 */
	Boolean existsByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin, Integer timeEnd);

	/**
	 * 获取课程直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return 课程直播
	 */
	CourseLive findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
			Integer timeEnd);

    /**
     * 寻找教师未确认的直播
     * @param teacher 教师
     * @return 直播列表
     */
	List<TaskLiveVo> findTaskTeacher(Teacher teacher);

	/**
	 * 获取教师课表
	 * 
	 * @param examTypes
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param dateBegin
	 *            日期开始
	 * @param dateEnd
	 *            日期结束
	 * @return 课程直播
	 */
	Page<CourseLiveScheduleVo> schedule(List<ExamType> examTypes, Long subjectId, Date dateBegin, Date dateEnd,
										Long teacherId , Pageable page);

    /**
     * 修改教师任务状态
     * @param teacher 教师
     * @param liveIds 直播ids
     * @param courseConfirmStatus 确认状态
     * @return 成功条数
     */
    int updateTaskTeacher(Teacher teacher, List<Long> liveIds, CourseConfirmStatus courseConfirmStatus);

	/**
	 * 滚动排课查询
	 * 
	 * @param currentCourseId
	 *            当前课程ID
	 * @param dates
	 *            日期
	 * @param courseId
	 *            课程ID
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 */
	List<CourseLiveRollingVo> findForRolling(Long currentCourseId, List<Date> dates, Long courseId, ExamType examType,
			Long subjectId);

	/**
	 * 修改直播绑定直播间
	 *
	 * @param liveId 直播id
	 * @param roomId 直播间id
	 * @return
	 */
    int updateLiveRoom(Long liveId, Long roomId);

    /**
     * 滚动排课
     * @param courseId
     * @param courseLiveIds
     */
	void rollingSchedule(Long courseId, List<Long> courseLiveIds);

	/**
	 * 修改直播绑定助教(主持人等)
	 * @param liveId 直播id
	 * @param teacherId 教师id
	 * @param teacherType 教师类型
	 * @return 条数
	 */
    int updateAssistant(Long liveId, Long teacherId, TeacherType teacherType);

	/**
	 * 根据课程ID和日期查询直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期
	 * @return 操作影响数
	 */
	List<CourseLive> findByCourseIdAndDateIn(Long courseId, List<Date> dates);

	/**
	 * 修改直播名称
	 * @param liveId 直播id
	 * @param liveName 直播名称
	 * @return 结果
	 */
    int updateLiveName(Long liveId, String liveName);

	/**
	 * 一键排课
	 * 
	 * @param id
	 *            课程ID
	 */
	void oneKeySchedule(Long id);

	/**
	 * 一键排课
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期
	 */
	void oneKeySchedule(Long courseId, List<Date> dates);

	/**
	 * 根据课程直播id查询状态 如果为直播安排 抛出异常
	 * @param liveId
	 */
	void findCourseStatusByCourseLiveId(Long liveId);

	/**
	 * 助教课表
	 * 
	 * @param courseName
	 *            课程名称
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param liveRoomId
	 *            直播间ID
	 * @param dateBegin
	 *            开始日期
	 * @param dateEnd
	 *            结束日期
	 * @param teacherType
	 *            教师类型
	 * @param teacherName
	 *            教师姓名
	 * @return 助教课表
	 */
	Page<CourseLiveScheduleAssistantVo> scheduleAssistant(String courseName, ExamType examType, Long subjectId,
			Long liveRoomId, Date dateBegin, Date dateEnd, TeacherType teacherType, String teacherName,Pageable page);

	/**
	 * 我的课表（助教）
	 * 
	 * @param courseName
	 *            课程名称
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param liveRoomId
	 *            直播间
	 * @param dateBegin
	 *            开始
	 * @param dateEnd
	 *            结束日期
	 * @param teacherType
	 *            助教类型
	 * @param teacherId
	 *            教师ID
	 * @return 课表
	 */
	List<CourseLiveScheduleAssistantVo> myScheduleAssistant(String courseName, ExamType examType, Long subjectId,
			Long liveRoomId, Date dateBegin, Date dateEnd, TeacherType teacherType, Long teacherId);

	/**
	 * 我的课表（讲师）
	 * 
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param dateBegin
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @param courseName
	 *            课程名称
	 * @param liveRoomId
	 *            直播间ID
	 * @param id
	 *            教师ID
	 * @return 课表
	 */
	List<CourseLiveScheduleVo> mySchedule(ExamType examType, Long subjectId, Date dateBegin, Date dateEnd,
			String courseName, Long liveRoomId, Long id);

	/**
	 * 根据课程ID日期和时间查询直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return 课程直播
	 */
	List<CourseLive> findByCourseIdAndDateInAndTimeBeginAndTimeEnd(Long courseId, List<Date> dates, Integer timeBegin,
			Integer timeEnd);

	/**
	 * 根据课程id 查找课程科目id
	 * @param courseLiveId
	 * @return
	 */
	Long getCourseSubjectId(Long courseLiveId);

	/**
	 * 通过日期和教师id查找直播
	 * @param dateBegin 开始日期
	 * @param dateEnd 结束日期
	 * @param teacherId 教师id
	 * @return 直播集合
	 */
	List<CourseLive> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId,Pageable page);

    int updateCourseLiveCategoryByStatus(Long liveId, CourseLiveCategory courseLiveCategory);

	/**
	 * 获取未确认的直播
	 */
	List<TaskLiveDGTVo> getLiveByDGT();

	void submitCourseLiveTeacherDGT(Long liveId, Long liveTeacherId, TeacherType teacherType, Long teacherIdl, TeacherCourseLevel level);
}
