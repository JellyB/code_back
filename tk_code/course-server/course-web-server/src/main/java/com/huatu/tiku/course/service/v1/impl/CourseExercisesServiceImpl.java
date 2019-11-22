package com.huatu.tiku.course.service.v1.impl;

import com.google.common.base.Stopwatch;
import com.huatu.tiku.course.service.v1.CourseExercisesService;
import com.huatu.tiku.course.service.cache.CacheUtil;
import com.huatu.tiku.course.service.cache.CourseExercisesCacheKey;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.question.QuestionServiceV1;
import com.huatu.tiku.entity.CourseExercisesQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/21
 */
@Slf4j
@Service
public class CourseExercisesServiceImpl extends BaseServiceHelperImpl<CourseExercisesQuestion> implements CourseExercisesService {

    public CourseExercisesServiceImpl() {
        super(CourseExercisesQuestion.class);
    }

    @Autowired
    private QuestionServiceV1 questionServiceV1;

    @Autowired
    private CacheUtil cacheUtil;

    @Override
    public List<Map<String, Object>> listQuestionByCourseId(final Integer courseType, final Long courseId) {
        //1 获取key
        Supplier keySupplier = () -> CourseExercisesCacheKey.CourseExercisesKey(courseType, courseId);
        //2 获取value
        Supplier<List<Map<String, Object>>> valueSupplier = () -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            //2.1.获取所有的ID
            WeekendSqls<CourseExercisesQuestion> sql = WeekendSqls.custom();
            sql.andEqualTo(CourseExercisesQuestion::getCourseId, courseId);
            sql.andEqualTo(CourseExercisesQuestion::getCourseType, courseType);
            Example example = Example.builder(CourseExercisesQuestion.class)
                    .where(sql)
                    .orderBy(" sort")
                    .build();
            final List<CourseExercisesQuestion> courseExercisesQuestionList = selectByExample(example);
            if (courseExercisesQuestionList.size() == 0){
                return null;
            }
            log.info("获取课后习题ID ===》{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            //2.2 获取试题详情
            String questionIds = courseExercisesQuestionList.stream()
                    .map(CourseExercisesQuestion::getQuestionId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            Object listQuestionByIds = questionServiceV1.listQuestionByIds(questionIds);
            List<Map<String, Object>> result = (List<Map<String, Object>>) ZTKResponseUtil.build(listQuestionByIds);
            log.info("获取课后习题-试题详情 ===》{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return result;
        };
        List<Map<String, Object>> cacheStringValue = cacheUtil.getCacheStringValue(keySupplier, valueSupplier, 30, TimeUnit.MINUTES);
        return cacheStringValue;
    }
}
