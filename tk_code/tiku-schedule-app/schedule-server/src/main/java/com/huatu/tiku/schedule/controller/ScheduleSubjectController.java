package com.huatu.tiku.schedule.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.entity.ScheduleSubject;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.service.ScheduleSubjectService;
import com.huatu.tiku.schedule.util.PageUtil;
import com.huatu.tiku.schedule.util.ResponseMsg;

/**
 * 科目控制器
 *
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("subject")
public class ScheduleSubjectController {

	@Autowired
	private ScheduleSubjectService scheduleSubjectService;

	/**
	 * 新增科目
	 * 
	 * @param subject
	 *            科目
	 * @return ResponseMsg
	 */
	@PostMapping
	public ResponseMsg<ScheduleSubject> save(@RequestBody ScheduleSubject subject) {
		if (subject.getExamType() == null) {
			return ResponseMsg.error(400, "考试类型不能为空");
		}
		if (subject.getName() == null) {
			return ResponseMsg.error(400, "名称不能为空");
		}
		if (subject.getStatus() == null) {
			return ResponseMsg.error(400, "状态不能为空");
		}
		scheduleSubjectService.save(subject);
		return ResponseMsg.success(subject);
	}

	/**
	 * 查询科目
	 *
	 * @param examType
	 *            考试类型
	 * @return 科目列表
	 */
	@GetMapping
	public PageUtil<ScheduleSubject> findByExamTypeAndName(ScheduleExamType examType, String name, Pageable page) {
		return scheduleSubjectService.findByExamTypeAndName(examType, name, page);
	}

	/**
	 * 更新状态
	 * 
	 * @param subject
	 *            科目
	 * @return ResponseMsg
	 */
	@PostMapping("updateStatus")
	public ResponseMsg<Void> updateStatus(@RequestBody ScheduleSubject subject) {
		if (subject.getId() == null) {
			return ResponseMsg.error(400, "ID不能为空");
		}
		if (subject.getStatus() == null) {
			return ResponseMsg.error(400, "状态不能为空");
		}
		int result = scheduleSubjectService.updateStatus(subject.getId(), subject.getStatus());
		return result == 1 ? ResponseMsg.success() : ResponseMsg.error(404);
	}
}
