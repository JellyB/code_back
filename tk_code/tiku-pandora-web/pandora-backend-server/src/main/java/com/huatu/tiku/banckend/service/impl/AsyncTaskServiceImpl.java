package com.huatu.tiku.banckend.service.impl;

import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.banckend.service.CourseExercisesService;
import com.huatu.tiku.dto.request.CourseCheckVO;
import com.huatu.tiku.dto.request.UpdateExerciseNumReqVO;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseExercisesQuestion;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zhaoxi
 * @Description: 异步任务
 **/
@Slf4j
@Component
public class AsyncTaskServiceImpl {
    @Autowired
    private CourseExercisesService courseExercisesService;
    @Autowired
    private CourseBreakpointService courseBreakpointService;
    @Autowired
    private CourseBreakpointQuestionService courseBreakpointQuestionService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${updateQuestionCountUrl}")
    private String updateQuestionCountUrl;

    /**
     * 异步方法统计课程绑定的题目个数，并通知PHP
     *
     * @param courseType
     * @param courseId
     * @return
     */
    @Async
    public void upQuestionNumOfCourse(Integer courseType, Long courseId) {
        //1.统计题目数量
        Integer afterExercisesCount = getCourseExerciseQuestionCount(courseType, courseId);
        Integer classExercisesCount = getBreakpointQuestionCount(courseType, courseId);


        //2.通知PHP更新题目个数
        UpdateExerciseNumReqVO vo = UpdateExerciseNumReqVO.builder()
                .afterExercisesNum(afterExercisesCount)
                .classExercisesNum(classExercisesCount)
                .classId(courseId)
                .courseType(courseType)
                .questionType(0)
                .subjectType(1)//1 行测 2 申论
                .build();

        restTemplate.postForObject(updateQuestionCountUrl, vo, ResponseMsg.class);
    }

    /**
     * 异步方法统计课程绑定的题目个数，并通知PHP(批量)
     */
    public void upQuestionNumOfCourseBatch(Map<Long, CourseCheckVO> courseInfoMap) {
        List<UpdateExerciseNumReqVO> list = new LinkedList<>();
        courseInfoMap.values().forEach(courseInfo -> {
            Long courseId = courseInfo.getId();
            int courseType = courseInfo.getType();
            UpdateExerciseNumReqVO vo = UpdateExerciseNumReqVO.builder()
                    .afterExercisesNum(getCourseExerciseQuestionCount(courseType, courseId))
                    .classExercisesNum(getBreakpointQuestionCount(courseType, courseId))
                    .classId(courseId)
                    .courseType(courseType)
                    .questionType(0)
                    .subjectType(1)
                    .build();
            list.add(vo);
        });
        log.info("php请求参数是:{}", JsonUtil.toJson(list));

        //对中文格式数据进行处理
        FormHttpMessageConverter fc = new FormHttpMessageConverter();
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        List<HttpMessageConverter<?>> partConverters = new ArrayList<HttpMessageConverter<?>>();
        partConverters.add(stringConverter);
        partConverters.add(new ResourceHttpMessageConverter());
        fc.setPartConverters(partConverters);
        restTemplate.getMessageConverters().addAll(Arrays.asList(fc, new MappingJackson2HttpMessageConverter()));

        //发送请求，设置请求返回数据格式为String（去除上面方法中使用的httpEntity）
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(updateQuestionCountUrl, popHeaders(list), String.class);

    }

    //组装请求体
    protected MultiValueMap<String, String> popHeaders(Object obj) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();

        map.add("data", JsonUtil.toJson(obj));
        return map;
    }

    /**
     * 获取随堂练习题目数
     *
     * @param courseType
     * @param courseId
     * @return
     */
    public Integer getBreakpointQuestionCount(Integer courseType, Long courseId) {
        WeekendSqls<CourseBreakpoint> classExercisesSql = WeekendSqls.custom();
        classExercisesSql.andEqualTo(CourseBreakpoint::getCourseId, courseId);
        classExercisesSql.andEqualTo(CourseBreakpoint::getCourseType, courseType);
//        classExercisesSql.andNotEqualTo(CourseBreakpoint::getPosition, -1);

        Example classExercisesExample = Example.builder(CourseBreakpoint.class)
                .where(classExercisesSql)
                .build();
        List<CourseBreakpoint> courseBreakpoints = courseBreakpointService.selectByExample(classExercisesExample);

        int classExercisesCount = 0;
        //b.根据节点信息查询题目个数
        if (CollectionUtils.isNotEmpty(courseBreakpoints)) {
            List<Long> pointIdList = new LinkedList<>();
            courseBreakpoints.forEach(i -> {
                pointIdList.add(i.getId());
            });
            if (CollectionUtils.isNotEmpty(pointIdList)) {
                classExercisesCount = courseBreakpointQuestionService.selectCountByPointIdList(pointIdList);
            }
        }

        return classExercisesCount;
    }


    /**
     * 获取课后练习题目数
     *
     * @param courseType
     * @param courseId
     * @return
     */
    public Integer getCourseExerciseQuestionCount(Integer courseType, Long courseId) {
        //1-2课后习题个数
        WeekendSqls<CourseExercisesQuestion> afterExercisesSql = WeekendSqls.custom();
        afterExercisesSql.andEqualTo(CourseExercisesQuestion::getCourseId, courseId);
        afterExercisesSql.andEqualTo(CourseExercisesQuestion::getCourseType, courseType);

        Example afterExercisesExample = Example.builder(CourseExercisesQuestion.class)
                .where(afterExercisesSql)
                .build();
        int afterExercisesCount = courseExercisesService.selectCountByExample(afterExercisesExample);

        return afterExercisesCount;
    }
}
