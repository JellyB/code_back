package com.huatu.tiku.schedule.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;

/**
 * 考试类型控制器
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("examType")
public class ScheduleExamTypeController {

	/**
	 * 获取考试类型
	 * 
	 * @return 考试类型KVS
	 */
	@GetMapping
	public List<Map<String, String>> examTypes() {
		List<Map<String, String>> examTypes = new ArrayList<>();
		for (ScheduleExamType scheduleExamType : ScheduleExamType.values()) {
			Map<String, String> kvs = new HashMap<>();
			kvs.put("value", scheduleExamType.name());
			kvs.put("text", scheduleExamType.getText());
			examTypes.add(kvs);
		}

		return examTypes;
	}
}
