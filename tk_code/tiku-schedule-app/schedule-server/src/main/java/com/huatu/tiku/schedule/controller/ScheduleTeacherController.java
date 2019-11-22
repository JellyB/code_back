package com.huatu.tiku.schedule.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.entity.ScheduleTeacher;
import com.huatu.tiku.schedule.entity.ScheduleTeacherSubject;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.entity.enums.ScheduleTeacherStatus;
import com.huatu.tiku.schedule.service.ScheduleTeacherService;
import com.huatu.tiku.schedule.util.PageUtil;
import com.huatu.tiku.schedule.util.ResponseMsg;

/**
 * 老师控制器
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("teacher")
public class ScheduleTeacherController {

	@Autowired
	private ScheduleTeacherService teacherService;

	/**
	 * 获取老师状态
	 * 
	 * @return 老师状态KVS
	 */
	@GetMapping("status")
	public List<Map<String, String>> status() {
		List<Map<String, String>> teacherStatuses = new ArrayList<>();
		for (ScheduleTeacherStatus teacherStatus : ScheduleTeacherStatus.values()) {
			Map<String, String> kvs = new HashMap<>();
			kvs.put("value", teacherStatus.name());
			kvs.put("text", teacherStatus.getText());
			teacherStatuses.add(kvs);
		}

		return teacherStatuses;
	}

	/**
	 * 新增老师
	 * 
	 * @param teacher
	 *            老师
	 * @return ResponseMsg
	 */
	@PostMapping
	public ResponseMsg<ScheduleTeacher> save(@RequestBody ScheduleTeacher teacher) {
		if (teacher.getName() == null) {
			return ResponseMsg.error(400, "姓名不能为空");
		}
		if (teacher.getPhone() == null) {
			return ResponseMsg.error(400, "手机号不能为空");
		}
		if (teacher.getWechat() == null) {
			return ResponseMsg.error(400, "微信号不能为空");
		}
		if (teacher.getExamType() == null) {
			return ResponseMsg.error(400, "考试类型不能为空");
		}
		if (teacher.getSubjectId() == null) {
			return ResponseMsg.error(400, "科目不能为空");
		}
		if (teacher.getSubjectId() == null) {
			return ResponseMsg.error(400, "类型（组长/组员）不能为空");
		}

		if (teacher.getTeacherSubjects() == null) {
			return ResponseMsg.error(400, "讲授科目不能为空");
		} else {
			for (ScheduleTeacherSubject teacherSubject : teacher.getTeacherSubjects()) {
				if (teacherSubject.getTeacherLevelId() == null) {
					return ResponseMsg.error(400, "老师级别不能为空");
				}
			}
		}

		teacher.setStatus(ScheduleTeacherStatus.DSH);
		teacherService.save(teacher);

		return ResponseMsg.success(teacher);
	}

	/**
	 * 查询老师 TODO 具体条件按待确定
	 * 
	 * @param id
	 *            ID
	 * @param examType
	 *            考试类型
	 * @param subjectId
	 *            科目ID
	 * @param type
	 *            组长
	 * @param name
	 *            姓名
	 * @param status
	 *            状态
	 * @param page
	 *            分页信息
	 * @return 老师集合
	 */
	@GetMapping("findByCondition")
	public PageUtil<ScheduleTeacher> findByCondition(Long id, ScheduleExamType examType, Long subjectId, Boolean type,
			String name, ScheduleTeacherStatus status, Pageable page) {
		return teacherService.findByCondition(id, examType, subjectId, type, name, status, page);
	}

	/**
	 * 更新状态
	 * 
	 * @param teacher
	 *            老师
	 * @return ResponseMsg
	 */
	@PostMapping("updateStatus")
	public ResponseMsg<Void> updateStatus(@RequestBody ScheduleTeacher teacher) {
		if (teacher.getId() == null) {
			return ResponseMsg.error(400, "ID不能为空");
		}
		if (teacher.getStatus() == null) {
			return ResponseMsg.error(400, "状态不能为空");
		}
		int result = teacherService.updateStatus(teacher.getId(), teacher.getStatus());
		return result == 1 ? ResponseMsg.success() : ResponseMsg.error(404);
	}
}
