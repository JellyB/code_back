package com.huatu.tiku.schedule.biz.controller;

import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.SubjectService;
import com.huatu.tiku.schedule.biz.vo.SubjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 科目Controller
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("subject")
public class SubjectController {

	private final SubjectService subjectService;

	@Autowired
	public SubjectController(SubjectService subjectService) {
		this.subjectService = subjectService;
	}

	/**
	 * 根据考试类型查询科目
	 *
	 * @param examType
	 *            考试类型
	 * @return 科目列表
	 */
	@GetMapping("findByExamType")
	public List<SubjectVo> findByExamType(ExamType examType) {

        List<Subject> subjects = subjectService.findByExamType(examType);
        List<SubjectVo> subjectVos=new ArrayList();
        subjects.forEach(subject->{
            SubjectVo subjectVo=new SubjectVo(subject);
            subjectVos.add(subjectVo);
        });
        return subjectVos;
	}

    /**
     * 根据考试类型和科目查询科目
     *
     * @param examType
     *            考试类型
     * @return 科目列表
     */
    @GetMapping("findByExamTypeAndSubjectId")
    public List<SubjectVo> findByExamTypeAndSubjectId(ExamType examType,Long subjectId) {
		List<Subject> subjects = subjectService.findByExamType(examType,subjectId);
		List<SubjectVo> subjectVos=new ArrayList();
		subjects.forEach(subject->{
			SubjectVo subjectVo=new SubjectVo(subject);
			subjectVos.add(subjectVo);
		});
		return subjectVos;
    }


}
