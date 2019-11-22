package com.huatu.tiku.schedule.service;

import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.entity.ScheduleTeacher;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.entity.enums.ScheduleTeacherStatus;
import com.huatu.tiku.schedule.util.PageUtil;

/**
 * 老师Service
 * 
 * @author Geek-S
 *
 */
public interface ScheduleTeacherService {

	/**
	 * 新增老师
	 * 
	 * @param teacher
	 *            老师
	 */
	ScheduleTeacher save(ScheduleTeacher teacher);

	/**
	 * 查询老师
	 * 
	 * @param id
	 *            ID
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param type
	 *            组长
	 * @param name
	 *            姓名
	 * @param status
	 *            状态
	 * @param page
	 *            分页信息
	 * @return 老师集合
	 */
	PageUtil<ScheduleTeacher> findByCondition(Long id, ScheduleExamType examType, Long subjectId, Boolean type,
			String name, ScheduleTeacherStatus status, Pageable page);

	/**
	 * 更新状态
	 * 
	 * @param id
	 *            ID
	 * @param status
	 *            状态
	 * @return 影响结果数
	 */
	int updateStatus(Long id, ScheduleTeacherStatus status);

}
