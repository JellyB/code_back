package com.huatu.tiku.schedule.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.entity.ScheduleTeacherLevel;
import com.huatu.tiku.schedule.service.ScheduleTeacherLevelService;

/**
 * 老师等级控制器
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("teacherLevel")
public class ScheduleTeacherLevelController {

	@Autowired
	private ScheduleTeacherLevelService teacherLevelService;

	/**
	 * 查询所有老师等级
	 * 
	 * @return 老师等级列表
	 */
	@GetMapping
	public List<ScheduleTeacherLevel> findAllOrderBySort() {
		return teacherLevelService.findAllOrderBySort();
	}
}
