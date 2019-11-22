package com.huatu.tiku.schedule.biz.api;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.LiveRoomBookingRecord;
import com.huatu.tiku.schedule.biz.service.LiveRoomBookingRecordService;
import com.huatu.tiku.schedule.biz.service.LiveRoomService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import com.huatu.tiku.schedule.biz.vo.LiveRoomBookingRecordVo;

/**
 * 直播间
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("api/liveRoom")
public class LiveRoomApi {

	private final LiveRoomService liveRoomService;

	private final LiveRoomBookingRecordService liveRoomBookingRecordService;

	public LiveRoomApi(LiveRoomService liveRoomService, LiveRoomBookingRecordService liveRoomBookingRecordService) {
		this.liveRoomService = liveRoomService;
		this.liveRoomBookingRecordService = liveRoomBookingRecordService;
	}

	/**
	 * 获取所有直播间
	 * 
	 * @return
	 */
	@GetMapping
	public List<Map<String, Object>> getAll() {
		List<Map<String, Object>> liveRooms = Lists.newArrayList();

		liveRoomService.findAll().forEach(liveRoom -> {
			liveRooms.add(ImmutableMap.of("id", liveRoom.getId(), "name", liveRoom.getName()));
		});

		return liveRooms;
	}

	/**
	 * 根据直播间查询记录
	 * 
	 * @param id
	 *            直播间ID
	 * @return 记录
	 */
	@GetMapping("{id}/record")
	public List<LiveRoomBookingRecordVo> getDataById(@PathVariable Long id) {
		Date date = Date.from(LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant());

		List<LiveRoomBookingRecord> liveRoomBookingRecords = liveRoomBookingRecordService
				.findByLiveRoomIdAndDateOrderByTimeBegin(id, date);

		List<LiveRoomBookingRecordVo> liveRoomBookingRecordVos = Lists.newArrayList();

		liveRoomBookingRecords.forEach(liveRoomBookingRecord -> {
			LiveRoomBookingRecordVo liveRoomBookingRecordVo = new LiveRoomBookingRecordVo();
			liveRoomBookingRecordVo.setDate(DateformatUtil.format2(liveRoomBookingRecord.getDate()));
			liveRoomBookingRecordVo.setDayOfWeek(DateformatUtil.format4(liveRoomBookingRecord.getDate()));
			liveRoomBookingRecordVo.setTimeBegin(TimeformatUtil.format(liveRoomBookingRecord.getTimeBegin()));
			liveRoomBookingRecordVo.setTimeEnd(TimeformatUtil.format(liveRoomBookingRecord.getTimeEnd()));
			liveRoomBookingRecordVo.setUsername(liveRoomBookingRecord.getUsername());
			liveRoomBookingRecordVo.setPhone(liveRoomBookingRecord.getPhone());

			liveRoomBookingRecordVos.add(liveRoomBookingRecordVo);
		});

		return liveRoomBookingRecordVos;
	}

}
