package com.huatu.tiku.schedule.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.huatu.tiku.schedule.entity.ScheduleTeacher;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.entity.enums.ScheduleTeacherStatus;
import com.huatu.tiku.schedule.repository.ScheduleTeacherRepository;
import com.huatu.tiku.schedule.repository.ScheduleTeacherSubjectRepository;
import com.huatu.tiku.schedule.service.ScheduleTeacherService;
import com.huatu.tiku.schedule.util.PageUtil;

@Service
public class ScheduleTeacherServiceImpl implements ScheduleTeacherService {

	@Autowired
	private ScheduleTeacherRepository teacherRepository;

	@Autowired
	private ScheduleTeacherSubjectRepository teacherSubjectRepository;

	@Override
	@Transactional
	public ScheduleTeacher save(ScheduleTeacher teacher) {
		teacherRepository.save(teacher);

		if (teacher.getTeacherSubjects() != null) {
			teacher.getTeacherSubjects().forEach(teacherSubject -> {
				teacherSubject.setTeacherId(teacher.getId());
				teacherSubjectRepository.save(teacherSubject);
			});
		}

		return teacher;
	}

	@Override
	public PageUtil<ScheduleTeacher> findByCondition(Long id, ScheduleExamType examType, Long subjectId, Boolean type,
			String name, ScheduleTeacherStatus status, Pageable page) {
		Specification<ScheduleTeacher> querySpecific = new Specification<ScheduleTeacher>() {
			@Override
			public Predicate toPredicate(Root<ScheduleTeacher> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<>();
				if (id != null) {
					predicates.add(criteriaBuilder.equal(root.get("id"), id));
				}
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
				}
				if (subjectId != null) {
					predicates.add(criteriaBuilder.equal(root.get("subjectId"), examType));
				}
				if (type != null) {
					predicates.add(criteriaBuilder.equal(root.get("type"), type));
				}
				if (!StringUtils.isEmpty(name)) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
				}
				if (status != null) {
					predicates.add(criteriaBuilder.equal(root.get("status"), status));
				}
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		Integer pageNumber = page.getPageNumber() > 0 ? page.getPageNumber() - 1 : page.getPageNumber();
		page = new PageRequest(pageNumber, page.getPageSize());

		Page<ScheduleTeacher> resultList = teacherRepository.findAll(querySpecific, page);

		return PageUtil.<ScheduleTeacher>builder().result(resultList.getContent())
				.next(resultList.getTotalElements() > page.getPageNumber() * page.getPageSize() ? 1 : 0)
				.total(resultList.getTotalElements()).totalPage(resultList.getTotalPages()).build();
	}

	@Override
	public int updateStatus(Long id, ScheduleTeacherStatus status) {
		return teacherRepository.updateStatusById(id, status);
	}

}
