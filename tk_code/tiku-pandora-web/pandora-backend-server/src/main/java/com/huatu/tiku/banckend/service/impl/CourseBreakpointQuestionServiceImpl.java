package com.huatu.tiku.banckend.service.impl;

import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/12
 */
@Service
public class CourseBreakpointQuestionServiceImpl extends BaseServiceImpl<CourseBreakpointQuestion> implements CourseBreakpointQuestionService {

    public CourseBreakpointQuestionServiceImpl() {
        super(CourseBreakpointQuestion.class);
    }

    @Override
    public int deleteByBreakpointId(Long breakpointId) {
        Example example = buildExampleByBreakpointId(breakpointId);
        return deleteByExample(example);
    }

    @Transactional(value = "backendTransactionManager")
    @Override
    public int deleteByBreakpointIdList(List<Long> breakpointIdList) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andIn(CourseBreakpointQuestion::getBreakpointId,breakpointIdList);
        Example example = Example.builder(CourseBreakpointQuestion.class)
                .where(sql)
                .build();
        return deleteByExample(example);
    }

    @Override
    public void changeSort(Long courseBreakpointId, List<CourseBreakpointQuestion> list) {
        boolean anyMatch = list.stream().anyMatch(data ->
                null == data.getQuestionId() || data.getQuestionId() <= 0
                        || null == data.getSort() || data.getSort() <= 0);
        if (anyMatch) {
            throwBizException("id或排列序号非法");
        }
        list.forEach(data -> {
            WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
            sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, courseBreakpointId);
            sql.andEqualTo(CourseBreakpointQuestion::getQuestionId, data.getQuestionId());
            Example example = Example.builder(CourseBreakpointQuestion.class)
                    .andWhere(sql)
                    .build();
            updateByExampleSelective(data, example);
        });
    }
    /**
     * 根据课程Id 构建查询条件
     */
    private Example buildExampleByBreakpointId(Long breakpointId) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, breakpointId);
        return Example.builder(CourseBreakpointQuestion.class)
                .where(sql)
                .build();
    }


    /**
     * 根据结点id查询题目个数
     */
    @Override
    public int selectCountByPointIdList(List<Long> pointIdList) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andIn(CourseBreakpointQuestion::getBreakpointId,pointIdList);
        Example example = Example.builder(CourseBreakpointQuestion.class)
                .where(sql)
                .build();
        return selectCountByExample(example);
    }

    @Override
    public Map<Long, Long> countQuestionNumGroupById(List<Long> pointIdList) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andIn(CourseBreakpointQuestion::getBreakpointId,pointIdList);
        Example example = Example.builder(CourseBreakpointQuestion.class)
                .where(sql)
                .build();
        List<CourseBreakpointQuestion> list = selectByExample(example);
        return list.stream().collect(Collectors.groupingBy(CourseBreakpointQuestion::getBreakpointId, Collectors.counting()));
    }
}
