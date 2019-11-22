package com.huatu.tiku.schedule.biz.service.imple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.repository.SubjectRepository;
import com.huatu.tiku.schedule.biz.service.SubjectService;

@Service
public class SubjectServiceImpl extends BaseServiceImpl<Subject, Long> implements SubjectService {

	private final SubjectRepository subjectRepository;

	@Autowired
	public SubjectServiceImpl(SubjectRepository subjectRepository) {
		this.subjectRepository = subjectRepository;
	}

	@Override
	public List<Subject> findByExamType(ExamType examType) {
		return subjectRepository.findByExamTypeAndShowFlagIsTrue(examType);
	}

	@Override
	public List<Subject> findByExamType(ExamType examType, Long subjectId) {
		if(subjectId==null){
			return subjectRepository.findByExamTypeAndShowFlagIsTrue(examType);
		}
		return subjectRepository.findById(subjectId);
	}

}
