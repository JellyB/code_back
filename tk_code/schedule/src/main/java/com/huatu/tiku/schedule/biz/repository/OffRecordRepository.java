package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.OffRecord;

/**
 * 请假记录Repository
 * 
 * @author Geek-S
 *
 */
public interface OffRecordRepository extends BaseRepository<OffRecord, Long> {

	/**
	 * 根据教师ID查询请假记录
	 * 
	 * @param teacherId
	 *            教师ID
	 * @param begin
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param page
	 *            分页信息
	 * @return 请假记录
	 */
	Page<OffRecord> findByTeacherIdAndDateBetweenOrderByDateDescTimeBegin(Long teacherId, Date begin, Date end, Pageable page);

	/**
	 * 根据教师ID和日期查询请假记录
	 * 
	 * @param teacherId
	 *            教师ID
	 * @param date
	 *            日期
	 * @return 请假记录
	 */
	List<OffRecord> findByTeacherIdAndDate(Long teacherId, Date date);
}
