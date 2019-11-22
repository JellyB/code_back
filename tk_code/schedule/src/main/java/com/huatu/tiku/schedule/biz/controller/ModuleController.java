package com.huatu.tiku.schedule.biz.controller;

import com.huatu.tiku.schedule.biz.domain.Module;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 科目Controller
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("module")
public class ModuleController {

	private final ModuleService moduleService;

	@Autowired
	public ModuleController(ModuleService moduleService) {
		this.moduleService = moduleService;
	}

	/**
	 * 根据考试类型查询模块
	 *
	 * @param examType
	 *            考试类型
	 * @return 模块列表
	 */
	@GetMapping("findByExamType")
	public List<Module> findByExamType(ExamType examType) {
		return moduleService.findByExamType(examType);
	}

	/**
	 * 根据科目查询模块
	 *
	 * @param subjectId
	 *            科目id
	 * @return 模块列表
	 */
	@GetMapping("findBySubject")
	public List<Module> findBySubject(Long subjectId) {
		return moduleService.findBySubjectId(subjectId);
	}


}
