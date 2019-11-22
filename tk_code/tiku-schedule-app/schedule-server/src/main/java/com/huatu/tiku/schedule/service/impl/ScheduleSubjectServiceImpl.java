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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.huatu.tiku.schedule.entity.ScheduleSubject;
import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.repository.ScheduleSubjectRepository;
import com.huatu.tiku.schedule.service.ScheduleSubjectService;
import com.huatu.tiku.schedule.util.PageUtil;

@Service
public class ScheduleSubjectServiceImpl implements ScheduleSubjectService {

	@Autowired
	private ScheduleSubjectRepository subjectRepository;

	@Override
	public List<ScheduleSubject> findSubjectByParentId(ScheduleExamType examType) {
		return subjectRepository.findByExamTypeOrderBySort(examType);
	}

	@Override
	public ScheduleSubject save(ScheduleSubject subject) {
		return subjectRepository.save(subject);
	}

	@Override
	public PageUtil<ScheduleSubject> findByExamTypeAndName(ScheduleExamType examType, String name, Pageable page) {
		Specification<ScheduleSubject> querySpecific = new Specification<ScheduleSubject>() {
			@Override
			public Predicate toPredicate(Root<ScheduleSubject> root, CriteriaQuery<?> criteriaQuery,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<>();
				if (examType != null) {
					predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
				}
				if (!StringUtils.isEmpty(name)) {
					predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
				}
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		Integer pageNumber = page.getPageNumber() > 0 ? page.getPageNumber() - 1 : page.getPageNumber();
		page = new PageRequest(pageNumber, page.getPageSize(), new Sort(Sort.Direction.ASC, "sort"));

		Page<ScheduleSubject> resultList = subjectRepository.findAll(querySpecific, page);

		return PageUtil.<ScheduleSubject>builder().result(resultList.getContent())
				.next(resultList.getTotalElements() > page.getPageNumber() * page.getPageSize() ? 1 : 0)
				.total(resultList.getTotalElements()).totalPage(resultList.getTotalPages()).build();
	}

	@Override
	public int updateStatus(Long id, Boolean status) {
		return subjectRepository.updateStatusById(id, status);
	}

}
