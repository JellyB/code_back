package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.LiveRoomBookingRecord;
import com.huatu.tiku.schedule.biz.repository.LiveRoomBookingRecordRepository;
import com.huatu.tiku.schedule.biz.service.LiveRoomBookingRecordService;

@Service
public class LiveRoomBookingRecordServiceImpl extends BaseServiceImpl<LiveRoomBookingRecord, Long>
        implements LiveRoomBookingRecordService {

    private LiveRoomBookingRecordRepository liveRoomBookingRecordRepository;

    public LiveRoomBookingRecordServiceImpl(LiveRoomBookingRecordRepository liveRoomBookingRecordRepository) {
        this.liveRoomBookingRecordRepository = liveRoomBookingRecordRepository;
    }

    @Override
    public List<LiveRoomBookingRecord> findByLiveRoomIdAndDateOrderByTimeBegin(Long id, Date date) {
        return liveRoomBookingRecordRepository.findByLiveRoomIdAndDateOrderByTimeBegin(id, date);
    }

}
