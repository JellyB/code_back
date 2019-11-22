package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
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
	public Page<OffRecord> findByTeacherIdAndDateBetween(Long teacherId, Date begin, Date end, Pageable page) {
		return offRecordRepository.findByTeacherIdAndDateBetweenOrderByDateDescTimeBegin(teacherId, begin, end, page);
	}

	@Override
	public boolean validateTime(Long teacherId, Date date, Integer timeBegin, Integer timeEnd) {
		// 获取当天所有请假记录
		List<OffRecord> offRecords = offRecordRepository.findByTeacherIdAndDate(teacherId, date);

		// 判断请假时间是否有冲突
		for (OffRecord offRecord : offRecords) {
			if ((timeEnd > offRecord.getTimeBegin() && timeEnd < offRecord.getTimeEnd())
					|| (timeBegin > offRecord.getTimeBegin() && timeBegin < offRecord.getTimeEnd())
					|| (timeBegin <= offRecord.getTimeBegin() && timeEnd >= offRecord.getTimeEnd())) {
				// 时间有冲突
				return false;
			}
		}

		return true;
	}

}
