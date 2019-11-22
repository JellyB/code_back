package com.huatu.tiku.schedule.service;

import java.util.List;

import com.huatu.tiku.schedule.entity.ScheduleTeacherLevel;

/**
 * 老师等级Service
 * 
 * @author Geek-S
 *
 */
public interface ScheduleTeacherLevelService {

	/**
	 * 查询所有老师等级
	 * 
	 * @return 老师等级列表
	 */
	List<ScheduleTeacherLevel> findAllOrderBySort();

}
