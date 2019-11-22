package com.huatu.tiku.schedule.service;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;
import com.huatu.tiku.schedule.biz.service.LiveRoomService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveRoomServiceT extends ScheduleApplicationTests {

	@Autowired
	private LiveRoomService liveRoomService;

	@Test
	public void getAvailable() {
		Integer date = 20180330;

		Integer timeBegin = 800;

		Integer timeEnd = 1100;

		List<LiveRoom> liveRooms = liveRoomService.getAvailable(date, timeBegin, timeEnd);

		liveRooms.forEach(liveRoom -> {
			log.info("LiveRoom name is {}", liveRoom.getName());
		});
	}
}
