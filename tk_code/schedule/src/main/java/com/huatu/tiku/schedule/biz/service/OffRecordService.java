package com.huatu.tiku.schedule.biz.service;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.OffRecord;

/**
 * 请假记录Service
 * 
 * @author Geek-S
 *
 */
public interface OffRecordService extends BaseService<OffRecord, Long> {

	/**
	 * 根据教师ID查询请假记录
	 * 
	 * @param teacherId
	 *            教师ID
	 * @param page
	 *            分页信息
	 * @return 请假记录
	 */
	Page<OffRecord> findByTeacherIdAndDateBetween(Long teacherId, Date begin, Date end, Pageable page);

	/**
	 * 校验请假时间是否正确
	 * 
	 * @param teacherId
	 *            教师ID
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return 是否正确
	 */
	boolean validateTime(Long teacherId, Date date, Integer timeBegin, Integer timeEnd);

}
