package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.RuleRepository;
import com.huatu.tiku.schedule.biz.service.RuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


/**
 * @author wangjian
 **/
@Service
public class RuleServiceImpl extends BaseServiceImpl<Rule, Long> implements RuleService {

    private final RuleRepository ruleRepository;

    public RuleServiceImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Page<Rule> getRuleList(Pageable page) {
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.DESC, "dateEnd"));//结束时间
        list.add(new Sort.Order(Sort.Direction.DESC, "dateBegin"));//开始时间
        Sort sort = new Sort(list);
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), sort);
        return ruleRepository.findAll(pageable);
//        return ruleRepository.findAll(page);
    }

    @Override
    public List<Rule> checkDate(Integer begin, Integer end) {
        return ruleRepository.checkDate(begin, end);
    }

    @Override
    public List<Rule> findRuleByData(Integer date, CourseCategory courseCategory, ExamType examType,
                                     CourseLiveCategory liveCategory, SchoolType schoolType) {
        Specification<Rule> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateBeginInt"), date));//大于开始日期
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEndInt"), date));//小于结束日期
            predicates.add(criteriaBuilder.equal(root.get("courseCategory"), courseCategory));//课程类型
            if(schoolType!=null){
                predicates.add(criteriaBuilder.equal(root.get("schoolType"), schoolType));
            }

            predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("examType"), examType),criteriaBuilder.equal(root.get("examType"), ExamType.ALL) ));//考试类型

            if (liveCategory != null) {//查找面试分类
                predicates.add(criteriaBuilder.equal(root.get("liveCategory"), liveCategory));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return ruleRepository.findAll(querySpecific,new Sort(Sort.Direction.ASC,"examType"));
    }

    @Override
    public List<Rule> findRuleByData(Integer date, CourseCategory courseCategory, ExamType examType,
                                     CourseLiveCategory liveCategory, SchoolType schoolType, TeacherType teacherType) {
        Specification<Rule> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateBeginInt"), date));//大于开始日期
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEndInt"), date));//小于结束日期
            predicates.add(criteriaBuilder.equal(root.get("courseCategory"), courseCategory));//课程类型
            predicates.add(criteriaBuilder.equal(root.get("teacherType"), teacherType));// 授课身份
            predicates.add(criteriaBuilder.equal(root.get("schoolType"), schoolType));

            predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("examType"), examType),criteriaBuilder.equal(root.get("examType"), ExamType.ALL) ));//考试类型

            predicates.add(criteriaBuilder.equal(root.get("liveCategory"), liveCategory));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return ruleRepository.findAll(querySpecific,new Sort(Sort.Direction.ASC,"examType"));
    }

    @Override
    public List<Rule> checkDateExceptId(Integer beginInt, Integer endInt, Long id) {
        return ruleRepository.checkDateExceptId(beginInt, endInt, id);
    }

    @Override
    public List<Rule> findVideoRuleByData(Integer dateInt) {
        Specification<Rule> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateBeginInt"), dateInt));//大于开始日期
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEndInt"), dateInt));//小于结束日期
            predicates.add(criteriaBuilder.equal(root.get("courseCategory"), CourseCategory.VIDEO));//课程类型

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return ruleRepository.findAll(querySpecific,new Sort(Sort.Direction.ASC,"examType"));
    }
}
