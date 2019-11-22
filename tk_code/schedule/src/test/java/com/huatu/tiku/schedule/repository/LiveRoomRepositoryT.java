package com.huatu.tiku.schedule.repository;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.LiveRoomRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveRoomRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private LiveRoomRepository liveRoomRepository;

	@Test
	public void getLiveRoomWithCourseLives() {
		Integer date = 20180429;

		Integer timeBegin = 800;

		Integer timeEnd = 1100;

		List<Long> liveRoomIds = liveRoomRepository.getUnavailableLiveRoomIds(date, timeBegin, timeEnd);

		log.info("LiveRoom is {}", liveRoomIds);
	}
}
