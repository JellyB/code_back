package com.huatu.tiku.schedule.biz.service.imple;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.repository.RuleRepository;
import com.huatu.tiku.schedule.biz.service.RuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author wangjian
 **/
@Service
public class RuleServiceImpl extends BaseServiceImpl<Rule, Long>  implements RuleService {

    private final RuleRepository ruleRepository;

    public RuleServiceImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Page<Rule> getRuleList(Pageable page) {
        return ruleRepository.findAll(page);
    }

    @Override
    public List<Rule> checkDate(Integer begin, Integer end) {
        return ruleRepository.checkDate(begin,end);
    }

    @Override
    public List<Rule> findRuleByData(Integer date, CourseCategory courseCategory, ExamType examType, CourseLiveCategory liveCategory) {
        Specification<Rule> querySpecific = new Specification<Rule>() {

            @Override
            public Predicate toPredicate(Root<Rule> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                predicates.add(criteriaBuilder.lessThan(root.get("dateBeginInt"), date));//大于开始日期
                predicates.add(criteriaBuilder.greaterThan(root.get("dateEndInt"), date));//小于结束日期
                predicates.add(criteriaBuilder.equal(root.get("courseCategory"), courseCategory));//课程类型
                predicates.add(criteriaBuilder.equal(root.get("examType"), examType));//考试类型
                if (liveCategory != null) {//查找面试分类
                    predicates.add(criteriaBuilder.equal(root.get("liveCategory"), liveCategory));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<Rule> all = ruleRepository.findAll(querySpecific);
        return all;
    }

    @Override
    public List<Rule> checkDateExceptId(Integer beginInt, Integer endInt, Long id) {
        return ruleRepository.checkDateExceptId(beginInt, endInt, id);
    }
}
