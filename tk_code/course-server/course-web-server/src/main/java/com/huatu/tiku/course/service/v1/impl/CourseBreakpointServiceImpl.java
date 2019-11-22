package com.huatu.tiku.course.service.v1.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.common.spring.cache.Cached;
import com.huatu.tiku.course.bean.CourseBreakpointQuestionDTO;
import com.huatu.tiku.course.service.cache.CacheUtil;
import com.huatu.tiku.course.service.cache.CourseBreakpointCacheKey;
import com.huatu.tiku.course.service.v1.CourseBreakpointQuestionService;
import com.huatu.tiku.course.service.v1.CourseBreakpointService;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/19
 */
@Slf4j
@Service
public class CourseBreakpointServiceImpl extends BaseServiceHelperImpl<CourseBreakpoint> implements CourseBreakpointService {

    public CourseBreakpointServiceImpl() {
        super(CourseBreakpoint.class);
    }

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private PracticeCardServiceV1 practiceCardService;

    @Autowired
    CourseBreakpointQuestionService courseBreakpointQuestionService;

    @Cached(name = "课程-断点信息",
            key = "T(com.huatu.tiku.course.service.cache.CourseBreakpointCacheKey).breakpointKey(#map)",
            params = {@Cached.Param(name = "courseType=?&courseId=?", value = "map", type = Map.class)})
    @Override
    public List<CourseBreakpointQuestionDTO> listAllQuestionId(int courseType, long courseId) {
        Supplier key = () -> CourseBreakpointCacheKey.breakpointKey(courseId, courseType);
        Supplier<List<CourseBreakpointQuestionDTO>> value = () -> {
            List<CourseBreakpoint> dataList = listByCourseTypeAndId(courseType, courseId);
            if (dataList == null) {
                return null;
            }
            //获取所有的端点ID
            List<Long> breakPointIdList = dataList.stream()
                    .map(CourseBreakpoint::getId)
                    .collect(Collectors.toList());
            final List<CourseBreakpointQuestion> questionIdList = courseBreakpointQuestionService.listQuestionIdByBreakpointIdList(breakPointIdList);
            //组装成DTO信息 - 包含试题信息
            List<CourseBreakpointQuestionDTO> courseBreakpointQuestionDTOS = dataList.stream()
                    .map(courseBreakpoint -> {
                        CourseBreakpointQuestionDTO dto = CourseBreakpointQuestionDTO.builder()
                                .id(courseBreakpoint.getId())
                                .pointName(courseBreakpoint.getPointName())
                                .position(courseBreakpoint.getPosition())
                                .sort(courseBreakpoint.getSort())
                                .build();
                        List<Long> questionList = questionIdList.stream()
                                .filter(courseBreakpointQuestion -> courseBreakpointQuestion.getBreakpointId().equals(courseBreakpoint.getId()))
                                .map(CourseBreakpointQuestion::getQuestionId)
                                .collect(Collectors.toList());
                        dto.setQuestionInfoList(questionList);
                        return dto;
                    })
                    .collect(Collectors.toList());
            return courseBreakpointQuestionDTOS;
        };
        //生成缓存
        return cacheUtil.getCacheStringValue(key, value, 5, TimeUnit.MINUTES);
    }

    /**
     * 从数据库获取断点信息
     *
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    @Override
    public List<CourseBreakpoint> listByCourseTypeAndId(Integer courseType, Long courseId) {
        //生成value
        WeekendSqls<CourseBreakpoint> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseBreakpoint::getCourseType, courseType);
        sql.andEqualTo(CourseBreakpoint::getCourseId, courseId);

        Example example = Example.builder(CourseBreakpoint.class)
                .where(sql)
                .orderBy("position ")
                .build();
        List<CourseBreakpoint> courseBreakpoints = selectByExample(example);
        if (CollectionUtils.isEmpty(courseBreakpoints)) {
            return Lists.newArrayList();
        }
        return courseBreakpoints;
    }

    @Override
    public List<CourseBreakpointQuestion> listQuestionByCourseTypeAndId(Integer courseType, Long courseId) {
        List<CourseBreakpoint> courseBreakpointList = listByCourseTypeAndId(courseType, courseId);
        if (CollectionUtils.isNotEmpty(courseBreakpointList)) {
            List<Long> beakPointIdList = courseBreakpointList.stream()
                    .map(CourseBreakpoint::getId)
                    .collect(Collectors.toList());
            List<CourseBreakpointQuestion> courseBreakpointQuestionList = courseBreakpointQuestionService.listQuestionIdByBreakpointIdList(beakPointIdList);
            return courseBreakpointQuestionList;
        }
        return Lists.newArrayList();
    }

    @Override
    public HashMap<String, Object> buildCard(int terminal, int subjectId, int userId, int courseType, long courseId) {
        List<CourseBreakpointQuestionDTO> list = listAllQuestionId(courseType, courseId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        //生成questionId
        String questionId = list.stream()
                .flatMap(data -> data.getQuestionInfoList().stream())
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        if (StringUtils.isBlank(questionId)) {
            return null;
        }
        //生成节点信息
        List<Object> breakPointInfoList = list.stream()
                .map(data -> JSONObject.toJSON(data))
                .collect(Collectors.toList());
        Object practiceCard = practiceCardService.createCourseBreakPointPracticeCard(
                terminal, subjectId, userId, "课中练习",
                courseType, courseId, questionId, breakPointInfoList
        );
        HashMap<String, Object> result = (HashMap<String, Object>) ZTKResponseUtil.build(practiceCard);
        if (null != result) {
            result.computeIfPresent("id", (key, value) -> String.valueOf(value));
        }
        return result;
    }
}
