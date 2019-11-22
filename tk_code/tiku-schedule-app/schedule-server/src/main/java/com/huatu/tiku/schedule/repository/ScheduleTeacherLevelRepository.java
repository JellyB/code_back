package com.huatu.tiku.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.huatu.tiku.schedule.entity.ScheduleSubject;
import com.huatu.tiku.schedule.entity.ScheduleTeacherLevel;

/**
 * 老师级别Repository
 */
public interface ScheduleTeacherLevelRepository extends BaseRepository<ScheduleSubject> {

	/**
	 * 查询所有老师等级
	 * 
	 * @return 老师等级列表
	 */
	@Query("select s from ScheduleTeacherLevel s order by s.sort")
	List<ScheduleTeacherLevel> findAllOrderBySort();
}
