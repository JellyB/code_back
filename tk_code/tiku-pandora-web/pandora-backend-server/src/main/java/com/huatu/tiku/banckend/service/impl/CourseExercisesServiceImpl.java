package com.huatu.tiku.banckend.service.impl;

import com.huatu.tiku.banckend.service.CourseExercisesService;
import com.huatu.tiku.entity.CourseExercisesQuestion;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;

/**
 * Created by lijun on 2018/6/12
 */
@Service
public class CourseExercisesServiceImpl extends BaseServiceImpl<CourseExercisesQuestion> implements CourseExercisesService {

    public CourseExercisesServiceImpl() {
        super(CourseExercisesQuestion.class);
    }

    @Override
    public int deleteQuestion(Long courseId, Integer courseType, Long questionId) {
        Example example = buildExample(courseId, courseType, questionId);
        return deleteByExample(example);
    }

    @Override
    public int changeSort(Long courseId, Integer courseType, List<CourseExercisesQuestion> list) {
        boolean anyMatch = list.stream().anyMatch(data -> null == data.getQuestionId() || data.getQuestionId() <= 0);
        if (anyMatch) {
            throwBizException("id或排列序号非法");
        }
        list.forEach(data -> {
            Example example = buildExample(courseId, courseType, data.getQuestionId());
            updateByExampleSelective(data, example);
        });
        return 0;
    }

    private Example buildExample(Long courseId, Integer courseType, Long questionId) {
        WeekendSqls<CourseExercisesQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseExercisesQuestion::getCourseType, courseType);
        sql.andEqualTo(CourseExercisesQuestion::getCourseId, courseId);
        sql.andEqualTo(CourseExercisesQuestion::getQuestionId, questionId);
        Example example = Example.builder(CourseExercisesQuestion.class)
                .where(sql)
                .build();
        return example;
    }

    public int deleteQuestionByCourseId(Long courseWareId, Integer courseType) {
        Example example = new Example(CourseExercisesQuestion.class);
        example.and().andEqualTo("courseId", courseWareId);
        example.and().andEqualTo("courseType", courseType);
        int result = deleteByExample(example);
        return result;
    }

}
