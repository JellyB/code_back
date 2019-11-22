package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.LiveRoomBookingRecord;

/**
 * 直播间预约记录Repository
 *
 * @author Geek-S
 */
public interface LiveRoomBookingRecordRepository extends BaseRepository<LiveRoomBookingRecord, Long> {

	/**
	 * 根据直播间&时间查询记录
	 * 
	 * @param id
	 *            直播间ID
	 * @param date
	 *            日期
	 * @return 直播间预约记录
	 */
	List<LiveRoomBookingRecord> findByLiveRoomIdAndDateOrderByTimeBegin(Long id, Date date);

}
