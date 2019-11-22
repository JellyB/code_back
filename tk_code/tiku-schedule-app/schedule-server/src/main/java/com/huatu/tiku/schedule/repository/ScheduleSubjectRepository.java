package com.huatu.tiku.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.entity.ScheduleSubject;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;

/**
 * 科目Repository
 */
public interface ScheduleSubjectRepository extends BaseRepository<ScheduleSubject> {

	/**
	 * 查询科目
	 * 
	 * @param examType
	 *            考试类型
	 * @return 科目列表
	 */
	List<ScheduleSubject> findByExamTypeOrderBySort(ScheduleExamType examType);

	/**
	 * 根据ID更新状态字段
	 * 
	 * @param id
	 *            ID
	 * @param status
	 *            状态
	 * @return 影响结果数
	 */
	@Transactional
	@Modifying
	@Query("update ScheduleSubject s set s.status = ?2 where s.id = ?1")
	int updateStatusById(Long id, Boolean status);
}
