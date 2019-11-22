package com.huatu.tiku.schedule.biz.service;

import java.util.List;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;

/**
 * 直播间Service
 * 
 * @author Geek-S
 *
 */
public interface LiveRoomService extends BaseService<LiveRoom, Long> {

	/**
	 * 获取可用直播间
	 * 
	 * @param date
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return 直播间列表
	 */
	List<LiveRoom> getAvailable(Integer date, Integer timeBegin, Integer timeEnd);

}
