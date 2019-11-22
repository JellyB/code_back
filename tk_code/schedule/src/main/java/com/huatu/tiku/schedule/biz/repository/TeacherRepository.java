package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;

/**
 * 教师Repository
 * 
 * @author Geek-S
 *
 */
public interface TeacherRepository extends BaseRepository<Teacher, Long> {
	//根据名字查询老师id
	@Query(value = "SELECT id from teacher where name like %?1%",nativeQuery = true)
	List<Long> getIdByNameLike(String name);
    /**
     * 根据id更新状态
     * @param ids 教师id
     * @param status 状态
     * @return 结果
     */
    @Transactional
    @Modifying
    @Query("update Teacher t set t.status = ?2 where t.id in (?1) and t.id<>?3")
    int updateTeacherStatus(List<Long> ids, TeacherStatus status,Long id);

    /**
     * 查找指定时间已占用教师
     * @param date 日期
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @return ids
     */
    @Query(value = "SELECT DISTINCT teacher_id from course_live  cl LEFT JOIN course_live_teacher clt on cl.id=clt.course_live_id where date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
    List<Long> getUnavailableTeacherIds(Date date, Integer timeBegin, Integer timeEnd);

	/**
	 * 查询符合考试类型要求教师
	 * @param examType 考试类型
	 * @param teacherCourseLevel 授课级别
	 * @return ids
	 */
	@Query(value = "SELECT DISTINCT tb.teacher_id FROM teacher_subject tb WHERE tb.exam_type = ?1 AND tb.teacher_course_level >= ?2",nativeQuery = true)
	List<Long> getlevelTeacherByExamType(int examType, int teacherCourseLevel);

    /**
	 * 获取讲师列表
	 *
	 * @param teacherType
	 *            教师类型
	 * @param teacherStatus
	 *            教师状态
	 * @return 教师列表
	 */
	List<Teacher> findByTeacherTypeAndStatus(TeacherType teacherType, TeacherStatus teacherStatus);

	//查找已经排课的助教ids
    @Query(value = "SELECT DISTINCT assistant_id from course_live   where date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
    List<Long> getUnavailableAssistantIds(Date date, Integer timeBegin, Integer timeEnd);

    //查找已经排课主持人的ids
    @Query(value = "SELECT DISTINCT compere_id from course_live   where date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
    List<Long> getUnavailableCompereIds(Date date, Integer timeBegin, Integer timeEnd);

    //查找已经排课的场控ids
    @Query(value = "SELECT DISTINCT controller_id from course_live   where date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
    List<Long> getUnavailableControllerIds(Date date, Integer timeBegin, Integer timeEnd);

    //查找已经排课的学习师ids
    @Query(value = "SELECT DISTINCT learning_teacher_id from course_live   where date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
    List<Long> getUnavailableLearningTeacherIds(Date date, Integer timeBegin, Integer timeEnd);

	/**
	 * 根据手机号查询教师
	 *
	 * @param phone
	 *            手机号
	 * @return 教师
	 */
	Teacher findByPhone(String phone);

	/**
	 * 获取教师角色（权限）
	 *
	 * @param id
	 *            教师ID
	 * @return 角色
	 */
	@Query(value = "select a.resource from teacher_roles tr join role_authorities ra on tr.role_id = ra.role_id join authority a on ra.authority_id = a.id where tr.teacher_id = ?1", nativeQuery = true)
	Set<String> getAuthorities(Long id);

	/**
	 * 根据考试类型和科目查询教师
	 * 
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目
	 * @return 教师列表
	 */
	List<Teacher> findByExamTypeAndSubjectId(ExamType examType, Long subjectId);

	/**
	 * 查询教师的不可用时间
	 * 
	 * @param id
	 *            教师ID
	 * @param date
	 *            日期
	 * @return 时间集合
	 */
	@Query(value = "select course_live.date date, course_live.time_begin time_begin, course_live.time_end time_end from course_live_teacher join course_live on course_live_teacher.course_live_id = course_live.id where course_live_teacher.teacher_id = ?1 and course_live.date = ?2 union select date, time_begin, time_end from off_record where teacher_id = ?1 and date = ?2", nativeQuery = true)
	List<Object[]> getUnavailableTime(Long id, Date date);

	/**
	 * 判断手机号是否存在
	 *
	 * @param phone
	 *            手机号
	 * @return 教师
	 */
	boolean existsByPhone(String phone);

	/**
	 * 根据教师ID获取角色列表
	 * 
	 * @param id
	 *            教师ID
	 * @return 结果
	 */
	@Query(value = "select role.id id, role.name, teacher.teacher_id checked from role left join (select * from teacher_roles where teacher_id = ?1) teacher on role.id = teacher.role_id;", nativeQuery = true)
	List<Object[]> getRolesById(Long id);

	/**
	 * 根据教师ID清空功能权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 操作结果
	 */
	@Modifying
	@Query(value = "delete from teacher_roles where teacher_id = ?1 and role_id not in (1,2,3,8)", nativeQuery = true)
	int clearRolesById(Long id);

	/**
	 * 根据教师ID添加功能权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 操作结果
	 */
	@Modifying
	@Query(value = "insert into teacher_roles (teacher_id, role_id) values (?1, ?2)", nativeQuery = true)
	int saveRolesById(Long id, Long roleId);

	/**
	 * 查询指定时间段 请假教师id集合
	 * @param date 日期
	 * @param timeBegin 开始时间
	 * @param timeEnd 结束时间
	 * @return 请假教师集合
	 */
	@Query(value = "SELECT DISTINCT teacher_id from off_record WHERE date= ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
	List<Long> getOffRecordIds(Date date, Integer timeBegin, Integer timeEnd);

	@Query(value = "SELECT DISTINCT (clt.teacher_id) AS teacherId, COUNT(clt.id)" +
			"FROM (" +
			"    SELECT cl.id AS course_live_id" +
			"    FROM course_live cl" +
			"     WHERE cl.date = ?2" +
			"     ) co INNER JOIN course_live_teacher  clt on co.course_live_id = clt.course_live_id" +
			"            WHERE clt.teacher_id IN (?1)" +
			"GROUP BY clt.teacher_id", nativeQuery = true)
	List<Object[]> getTeacherLiveCount(List<Long> ids, Date date);

	@Query(value = "SELECT DISTINCT(clt.teacher_id) AS teacherId, COUNT(clt.id)" +
			"FROM (" +
			"    SELECT cl.id AS course_live_id" +
			"    FROM course_live cl" +
			"     WHERE cl.date = ?2 AND cl.time_begin < ?3" +
			"     ) co INNER JOIN course_live_teacher  clt ON co.course_live_id = clt.course_live_id" +
			"            WHERE clt.teacher_id IN (?1)" +
			"GROUP BY clt.teacher_id", nativeQuery = true)
	List<Object[]> getTeacherLiveCountMorning(List<Long> ids, Date date, Integer timeMorning);

	@Query(value = "SELECT DISTINCT(clt.teacher_id) AS teacherId, COUNT(clt.id)" +
			"FROM (" +
			"    SELECT cl.id AS course_live_id" +
			"    FROM course_live cl" +
			"     WHERE cl.date = ?2 AND cl.time_end > ?3" +
			"     ) co INNER JOIN course_live_teacher  clt ON co.course_live_id = clt.course_live_id" +
			"            WHERE clt.teacher_id IN (?1)" +
			"GROUP BY clt.teacher_id", nativeQuery = true)
	List<Object[]> getTeacherLiveCountNight(List<Long> ids, Date date, Integer timeNight);

	/**
	 * 根据教师姓名查询教师
	 * @param name 教师姓名
	 * @return 教师
	 */
	Teacher findOneByName(String name);

	/**
	 * 根据教师姓名查询教师
	 * @param name 教师姓名
	 * @return 教师集合
	 */
	List<Teacher> findByNameIn(Set<String> name);
	/**
	 * 获取教师的数据权限ID
	 * @param id 教师ID
	 * @return 数据权限ID集合
	 */
	@Query(value = "select data_permission from teacher_data_permissions where teacher_id = ?1", nativeQuery = true)
	Set<Integer> findDataPermissionIdsById(Long id);

	/**
	 * 根据教师ID清空数据权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 操作结果
	 */
	@Modifying
	@Query(value = "delete from teacher_data_permissions where teacher_id = ?1", nativeQuery = true)
	int clearDataPermissionsById(Long id);

	/**
	 * 根据教师ID添加数据权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 操作结果
	 */
	@Modifying
	@Query(value = "insert into teacher_data_permissions (teacher_id, data_permission) values (?1, ?2)", nativeQuery = true)
	void saveDataPermissionsById(Long id, Long dataPermissionId);

	/**
	 * 根据php主键id查找
	 * @param pid php主键
	 */
	Teacher findByPid(Long pid);

	/**
	 * 根据pids修改状态
	 * @param pids pids
	 * @param status 状态
	 * @return 修改数量
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE teacher set status =?2 where pid in?1",nativeQuery = true)
	int updateStatusByPids(List<Long> pids, Integer status);


	/**
	 * 查询推荐教师
	 * @param courseId 课程id
	 * @return 推荐教师
	 */
	@Query(value = "SELECT t.id FROM  course_interview_teacher cit LEFT JOIN teacher t on t.id=cit.interview_teacher where course_id=?1",nativeQuery = true)
	List<Long> findTeacherByCourseId(Long courseId);
}
