package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.OffRecord;
import com.huatu.tiku.schedule.biz.repository.OffRecordRepository;
import com.huatu.tiku.schedule.biz.service.OffRecordService;

@Service
public class OffRecordServiceImpl extends BaseServiceImpl<OffRecord, Long> implements OffRecordService {

    private final OffRecordRepository offRecordRepository;

    public OffRecordServiceImpl(OffRecordRepository offRecordRepository) {
        this.offRecordRepository = offRecordRepository;
    }

    @Override
    public List<OffRecord> findByTeacherIdAndDateBetween(Long teacherId, Date begin, Date end, Pageable page) {
        return offRecordRepository.findByTeacherIdAndDate(teacherId, begin,end);
    }

    @Override
    public boolean validateTime(Long teacherId, Date timeBegin, Date timeEnd) {
        // 获取指定时间请假记录
        List<OffRecord> offRecords = offRecordRepository.findByTeacherIdAndDate(teacherId, timeBegin,timeEnd);

        return null == offRecords || offRecords.isEmpty();
    }

}
