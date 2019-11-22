package com.huatu.tiku.banckend.service.impl;

import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/11
 */
@Service
public class CourseBreakpointServiceImpl extends BaseServiceImpl<CourseBreakpoint> implements CourseBreakpointService {

    public CourseBreakpointServiceImpl() {
        super(CourseBreakpoint.class);
    }

    @Autowired
    private CourseBreakpointQuestionService courseBreakpointQuestionService;

    @Transactional(value = "backendTransactionManager")
    @Override
    public void deleteById(Long id) {
        //1.删除主表数据
        int resultNum = deleteByPrimaryKey(id);
        //2.删除断点 - 试题数据
        if (resultNum > 0) {
            courseBreakpointQuestionService.deleteByBreakpointId(id);
        }
    }

    @Transactional(value = "backendTransactionManager")
    @Override
    public void deleteByIdList(List<Long> idList) {
        //1.删除主表数据
        WeekendSqls<CourseBreakpoint> sql = WeekendSqls.custom();
        sql.andIn(CourseBreakpoint::getId, idList);
        Example example = Example.builder(CourseBreakpoint.class)
                .where(sql)
                .build();
        int resultNum = deleteByExample(example);
        if (resultNum > 0) {
            courseBreakpointQuestionService.deleteByBreakpointIdList(idList);
        }
    }

    @Transactional(value = "backendTransactionManager")
    @Override
    public void changeSort(List<CourseBreakpoint> list) {
        boolean anyMatch = list.stream().anyMatch(courseBreakpoint ->
                null == courseBreakpoint.getId() || courseBreakpoint.getId() <= 0
                        || null == courseBreakpoint.getSort() || courseBreakpoint.getSort() <= 0
        );
        if (anyMatch) {
            throwBizException("id或排列序号非法");
        }
        list.forEach(courseBreakpoint -> save(courseBreakpoint));
    }

    @Override
    public List<CourseBreakpoint> listData(int courseType, long courseId, String pointName, long position) {
        WeekendSqls<CourseBreakpoint> sql = WeekendSqls.custom();
        if (StringUtils.isNotBlank(pointName)) {
            sql.andLike(CourseBreakpoint::getPointName, "%" + pointName + "%");
        }
        sql.andEqualTo(CourseBreakpoint::getCourseId, courseId);
        sql.andEqualTo(CourseBreakpoint::getCourseType, courseType);
        if (0 < position) {
            sql.andEqualTo(CourseBreakpoint::getPosition, position);
        }
        Example example = Example.builder(CourseBreakpoint.class)
                .where(sql)
                .orderByAsc("position")//先按照时间点排序
                .orderByDesc("sort")//按照排列序号顺序排列
                .build();
        List<CourseBreakpoint> list = selectByExample(example);
        if (list.size() > 0){
            //插入数量
            List<Long> idList = list.stream().map(CourseBreakpoint::getId).collect(Collectors.toList());
            final Map<Long, Long> longLongMap = courseBreakpointQuestionService.countQuestionNumGroupById(idList);
            List<CourseBreakpoint> collect = list.stream()
                    .map(courseBreakpoint -> {
                        Long questionCount = longLongMap.getOrDefault(courseBreakpoint.getId(), 0L);
                        courseBreakpoint.setQuestionCount(questionCount);
                        return courseBreakpoint;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
        return list;
    }
}
