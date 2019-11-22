package com.huatu.tiku.schedule.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.entity.ScheduleSubject;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.util.PageUtil;

/**
 * 科目Service
 * 
 * @author Geek-S
 *
 */
public interface ScheduleSubjectService {

	/**
	 * 查询科目
	 * 
	 * @param scheduleExamType
	 *            考试类型
	 * @return 科目列表
	 */
	List<ScheduleSubject> findSubjectByParentId(ScheduleExamType examType);

	/**
	 * 保存科目
	 * 
	 * @param subject
	 *            科目信息
	 */
	ScheduleSubject save(ScheduleSubject subject);

	/**
	 * 根据考试类型和名称查询
	 * 
	 * @param examType
	 *            考试类型
	 * @param name
	 *            名称
	 * @param page
	 *            分页信息
	 * @return 科目列表
	 */
	PageUtil<ScheduleSubject> findByExamTypeAndName(ScheduleExamType examType, String name, Pageable page);

	/**
	 * 根据ID更新状态字段
	 * 
	 * @param id
	 *            ID
	 * @param status
	 *            状态
	 * @return 影响结果数
	 */
	int updateStatus(Long id, Boolean status);

}
