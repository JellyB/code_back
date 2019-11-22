package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.OffRecord;
import org.springframework.data.jpa.repository.Query;

/**
 * 请假记录Repository
 * 
 * @author Geek-S
 *
 */
public interface OffRecordRepository extends BaseRepository<OffRecord, Long> {


	/**
	 * 根据教师ID和日期查询请假记录
	 * @return 请假记录
	 */
	@Query(value = "SELECT * FROM off_record WHERE teacher_id = ?1 AND ( time_begin BETWEEN ?2 AND ?3 OR time_end BETWEEN ?2 AND ?3 OR ?2 BETWEEN time_begin and time_end)",nativeQuery = true)
	List<OffRecord> findByTeacherIdAndDate(Long teacherId, Date timeBegin,Date timeEnd);
}
