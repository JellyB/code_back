package com.huatu.tiku.schedule.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.entity.ScheduleTeacher;
import com.huatu.tiku.schedule.entity.enums.ScheduleTeacherStatus;

/**
 * 老师Repository
 */
public interface ScheduleTeacherRepository extends BaseRepository<ScheduleTeacher> {

	/**
	 * 更新状态
	 * 
	 * @param id
	 *            ID
	 * @param status
	 *            状态
	 * @return 影响结果数
	 */
	@Transactional
	@Modifying
	@Query("update ScheduleTeacher s set s.status = ?2 where s.id = ?1")
	int updateStatusById(Long id, ScheduleTeacherStatus status);

}
