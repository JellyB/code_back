package com.huatu.tiku.schedule.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.entity.ScheduleTeacherLevel;
import com.huatu.tiku.schedule.repository.ScheduleTeacherLevelRepository;
import com.huatu.tiku.schedule.service.ScheduleTeacherLevelService;

@Service
public class ScheduleTeacherLevelServiceImpl implements ScheduleTeacherLevelService {

	@Autowired
	private ScheduleTeacherLevelRepository teacherLevelRepository;

	@Override
	public List<ScheduleTeacherLevel> findAllOrderBySort() {
		return teacherLevelRepository.findAllOrderBySort();
	}

}
