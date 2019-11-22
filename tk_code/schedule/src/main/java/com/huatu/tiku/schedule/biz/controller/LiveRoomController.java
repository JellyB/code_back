package com.huatu.tiku.schedule.biz.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;
import com.huatu.tiku.schedule.biz.service.LiveRoomService;

/**
 * 直播间Controller
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("liveRoom")
public class LiveRoomController {

	private final LiveRoomService liveRoomService;

	@Autowired
	public LiveRoomController(LiveRoomService liveRoomService) {
		this.liveRoomService = liveRoomService;
	}

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
	@GetMapping("getAvailable")
	public List<LiveRoom> getAvailable(Integer date, Integer timeBegin, Integer timeEnd) {
		return liveRoomService.getAvailable(date, timeBegin, timeEnd);
	}

	/**
	 * 获取全部直播间
	 * 
	 * @return 直播间列表
	 */
	@GetMapping
	public List<Map<String, Object>> getAll() {
		List<Map<String, Object>> liveRooms = Lists.newArrayList();

		liveRoomService.findAll().forEach(liveRoom -> liveRooms.add(ImmutableMap.of("id", liveRoom.getId(), "name", liveRoom.getName())));

		return liveRooms;
	}

}
