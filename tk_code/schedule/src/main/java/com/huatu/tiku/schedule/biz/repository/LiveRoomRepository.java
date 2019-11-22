package com.huatu.tiku.schedule.biz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;

/**
 * 直播间Repository
 * 
 * @author Geek-S
 *
 */
public interface LiveRoomRepository extends BaseRepository<LiveRoom, Long> {

	/**
	 * 获取指定时间被占用的直播间id
	 * @return 直播间列表
	 */
	@Query(value = "SELECT distinct live_room_id from course_live where date_int = ?1 and (?2 BETWEEN time_begin and time_end or ?3 BETWEEN time_begin and time_end or time_begin BETWEEN ?2 and ?3 )",nativeQuery = true)
	List<Long> getUnavailableLiveRoomIds(Integer date, Integer timeBegin, Integer timeEnd);

    /**
     * 获取除指定id外的直播间
     * @param ids
     * @return
     */
	@Query(value = "SELECT * from live_room where id not in (?1)",nativeQuery = true)
	List<LiveRoom> getAllByIdNotIn(List<Long> ids);

	/**
	 * 根据名称查询直播间
	 * @param name 直播间名称
	 * @return 直播间
	 */
	LiveRoom findOneByName(String name);
}
