package com.huatu.tiku.schedule.biz.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.vo.OptionVo;
import com.huatu.tiku.schedule.biz.vo.TeacherVo;

/**
 * 教师Service
 * 
 * @author Geek-S
 *
 */
public interface TeacherService extends BaseService<Teacher, Long> {

    /**
     * 添加教师及授课信息
     * @param teacher 教师实体
     * @return teacher 教师属性
     */
    Teacher saveX(Teacher teacher,List<TeacherSubject> teacherSubjects);

    /**
     * 条件查询
     * @param examType 考试类型
     * @param name 教师名字
     * @param id 教师id
     * @param subjectId 课程id
     * @param leaderFlag 是否组长
     * @param status 状态
     * @param teacherType 类型
     * @param page 分页
     * @return 查询结果
     */
    Page<TeacherVo> getTeacherList(ExamType examType, String name, Long id, Long subjectId, Boolean leaderFlag, TeacherStatus status, TeacherType teacherType, Pageable page);

    /**
     * 更改教师状态
     */
    int updateTeacherStatus(List<Long> ids, TeacherStatus status,Long id);

	/**
	 * 获取可用教师
	 * 
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param teacherCourseLevel
	 *            授课级别
	 * @param courseId
	 * 			课程id
	 * @param moduleId
	 * 			模块id
	 * @return 教师列表
	 */
	List<TeacherScoreBean> getAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
			Long subjectId, TeacherCourseLevel teacherCourseLevel,Long courseId,Long moduleId);

	/**
	 * 智能获取可用教师
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param teacherCourseLevel
	 *            授课级别
	 * @return 教师列表
	 */
	List<TeacherScoreBean> autoGetAvailableTeachers(Date date, Integer timeBegin, Integer timeEnd, ExamType examType,
			Long subjectId, TeacherCourseLevel teacherCourseLevel,Long courseId,Long moduleId);

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

	/**
	 * 查找可用助教(学习师 场控等)
	 * @param date 日期
	 * @param timeBegin 开始时间
	 * @param timeEnd 结束时间
	 * @param teacherType 教师类型
	 * @return 助教列表
	 */
    List<TeacherScoreBean> getAvailableAssistant(Date date, Integer timeBegin, Integer timeEnd, TeacherType teacherType);

	/**
	 * 根据手机号查询教师
	 * 
	 * @param phone
	 *            手机号
	 * @return 教师
	 */
	Teacher findByPhone(String phone);

	/**
	 * 获取教师权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 角色
	 */
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
	 * 查找面试推荐教师
	 * @return 分页教师列表
	 */
	Page<TeacherVo> findInterviewTeacher(Pageable page);

	/**
	 * 获取角色列表
	 *
	 * @param id
	 *            教师ID
	 * @return 角色列表
	 */
	List<OptionVo> getRolesById(Long id);

	/**
	 * 获取角色列表 去除讲师助教 组长
	 *
	 * @param id
	 *            教师ID
	 * @return 角色列表
	 */
	List<OptionVo> getRolesByIdExclude(Long id);

	/**
	 * 更新角色
	 * 
	 * @param id
	 *            教师ID
	 * @param roleIds
	 *            角色IDs
	 */
	void updateRolesById(Long id, List<Long> roleIds);

    Boolean updateTeacher(Teacher teacher, List<TeacherSubject> subjects);

    void importTeacherCourse(List<List<List<String>>> importList, Long courseId);
    void importTeacherRollCourse(List<List<List<String>>> importList);
	/**
	 * 根据教师ID获取数据权限
	 * 
	 * @param id
	 *            教师ID
	 * @return 数据权限
	 */
	List<OptionVo> getDataPermissionsById(Long id);

	/**
	 * 根据教师ID更新权限
	 * 
	 * @param id
	 *            教师ID
	 * @param permissionIds
	 *            权限IDs
	 */
	void updatePermissionsById(Long id, List<List<Long>> permissionIds);

	/**
	 * 获取教师的数据权限ID
	 * 
	 * @param id
	 *            教师ID
	 * @return 数据权限ID集合
	 */
	Set<ExamType> findDataPermissionIdsById(Long id);

    Teacher findByPid(Long pid);

	/**
	 * 导入教师
	 * @param list 教师数据
	 */
	void importTeachers(List<List<List<String>>> list);

	/**
	 * 从php批量导入数据
	 * @param data teachers
	 */
	void importTeacherByPHP(List<PHPUpdateTeacherDto> data);

	/**
	 * 同步php批量修改审核状态
	 * @param pids pids
	 * @param status 状态
	 */
	int updateStatusByPids(List<Long> pids, Integer status);


}
