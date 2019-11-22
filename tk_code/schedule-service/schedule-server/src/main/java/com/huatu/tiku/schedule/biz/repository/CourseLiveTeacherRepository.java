package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;

import java.math.BigInteger;
import java.util.List;

/**
 * 课程直播教师Repository
 * 
 * @author Geek-S
 *
 */
public interface CourseLiveTeacherRepository extends BaseRepository<CourseLiveTeacher, Long> {

	@Transactional
	@Modifying
	@Query(nativeQuery = true,value = "DELETE from course_live_teacher where id=?1")
	void deleteById(Long id);

	/**
	 * 绑定课程直播的教师
	 * 
	 * @param courseLiveTeacherId
	 *            课程直播明细ID
	 * @param teacherId
	 *            教师ID
	 * @return 操作影响数
	 */
	@Modifying
	@Query(value = "update course_live_teacher set teacher_id = ?2,confirm=?3 where id = ?1 or source_id=?1", nativeQuery = true)
	@Transactional
	int bindTeacher(Long courseLiveTeacherId, Long teacherId,Integer confirm);

	/**
	 * 教师任务确认
	 * @param teacherId 教师id
	 * @param ids 教师直播id集合
	 * @param rollIds
	 * @return 确认个数
	 */
	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.confirm = ?3 where (t.courseLiveId in ?2 and t.teacherId=?1) or id in?4")
	int updateTaskTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus, List<Long> rollIds);

	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.confirm = ?3 where t.courseLiveId in ?2 and t.teacherId=?1")
	int updateTaskTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

	/**
	 * 修改科目
	 */
	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.subjectId = ?2 where t.id =?1 or t.sourceId=?1")
	int savaSubject(Long courseLiveTeacherId, Long subjectId);

	/**
	 * 修改授课级别
	 */
	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.teacherCourseLevel = ?2  where t.id =?1 or t.sourceId=?1")
	int savaLiveTeacherLevel(Long courseLiveTeacherId, TeacherCourseLevel teacherCourseLevel);

	/**
	 * 根据课程直播教师id查找课程状态
	 * @param courseLiveTeacherId 课程直播教师id
	 * @return 课程状态
	 */
	@Query(value = "SELECT c.status FROM course_live_teacher clt INNER JOIN course_live cl ON clt.course_live_id = cl.id INNER JOIN course c ON c.id = cl.course_id WHERE clt.id = ?1",nativeQuery = true)
	CourseStatus findCourseStatusByCourseLiveTeacherId(Long courseLiveTeacherId);

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

	/**
	 * 待沟通的任务
	 * @return data
	 */
	@Query(value = "SELECT * FROM course_live_teacher clt LEFT JOIN course_live cl ON cl.id = clt.course_live_id LEFT JOIN course c ON c.id = cl.course_id  WHERE clt.confirm >= 2  AND c.status >=2",nativeQuery = true)
    List<CourseLiveTeacher> getTaskDGT();

	/**
	 * 修改原教师绑定
	 */
	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.lastTeacherId=?1 where t.courseLiveId in ?2 and t.teacherId=?1")
	int updateLastTeacher(Long teacherId, List<Long> ids);

	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.lastTeacherId=?1 where (t.courseLiveId in ?2 and t.teacherId=?1)or id in ?3")
	int updateLastTeacher(Long teacherId, List<Long> ids, List<Long> rollIds);

	/**
	 * 根据liveid和teacherid查找滚动内容ids
	 */
	@Query(value = "SELECT clt2.id FROM course_live_teacher clt INNER JOIN course_live_teacher clt2 ON clt.id = clt2.source_id WHERE clt.course_live_id IN ?2 and clt.teacher_id=?1",nativeQuery = true)
	List<BigInteger> findByTeacherIdAndsAndSourceLiveId(Long teacherId, List<Long> ids);

	/**
	 * 根据id,源id和教师id确认任务
	 */
	@Transactional
	@Modifying
	@Query(value = "update CourseLiveTeacher t set t.confirm = ?2 where t.id =?1 or t.sourceId=?1")
	int updateTaskTeacher(Long id, CourseConfirmStatus courseConfirmStatus);

	int deleteByIdOrSourceId(Long id,Long sourceId);
}
