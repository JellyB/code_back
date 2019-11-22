package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.Rule;
import com.huatu.tiku.schedule.biz.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * @author wangjian
 **/
public interface RuleService extends BaseService<Rule, Long> {

    /**
     * 分页查询
     * @param page 分页参数
     * @return 结果
     */
    Page<Rule> getRuleList(Pageable page);

    /**
     * 校验时间冲突
     * @param begin 开始时间
     * @param end 结束时间
     * @return 结果集
     */
    List<Rule> checkDate(Integer begin, Integer end);

    List<Rule> findRuleByData(Integer date, CourseCategory courseCategory, ExamType examType
            , CourseLiveCategory liveCategory, SchoolType schoolType);

    List<Rule> findRuleByData(Integer date, CourseCategory courseCategory, ExamType examType
            , CourseLiveCategory liveCategory, SchoolType schoolType, TeacherType teacherType);

    List<Rule> checkDateExceptId(Integer beginInt, Integer endInt, Long id);

    List<Rule> findVideoRuleByData(Integer dateInt);
}
