package com.huatu.tiku.banckend.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.CourseExercisesService;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.banckend.service.impl.AsyncTaskServiceImpl;
import com.huatu.tiku.entity.CourseExercisesQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;

/**
 * 课件 - 课后练习
 * Created by lijun on 2018/6/12
 */
@RestController
@RequestMapping("/backend/courseExercises")
public class CourseExercisesController {

    @Autowired
    private CourseExercisesService courseExercisesService;
    @Autowired
    private AsyncTaskServiceImpl asyncTaskServiceImpl;
    @Autowired
    private CourseKnowledgeService courseKnowledgeService;

    /**
     * 编辑数据
     */
    @PostMapping(value = "edit")
    public Object edit(
            @Validated @RequestBody CourseExercisesQuestion courseExercisesQuestion
    ) {
        //设置默认的用户ID
        courseExercisesQuestion.setUserId(1L);
        courseExercisesService.save(courseExercisesQuestion);

        //根据试题更新课件知识点
        courseKnowledgeService.boundKnowledgeByQuestion(courseExercisesQuestion.getCourseId(), courseExercisesQuestion.getCourseType(), courseExercisesQuestion.getQuestionId());

        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseExercisesQuestion.getCourseType(), courseExercisesQuestion.getCourseId());

        return SuccessMessage.create();
    }

    /**
     * 查询某个课程下的课后练习试题信息
     */
    @GetMapping(value = "/{courseType}/{courseId}")
    public Object list(
            @PathVariable("courseId") Long courseId,
            @PathVariable("courseType") Integer courseType
    ) {
        WeekendSqls<CourseExercisesQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseExercisesQuestion::getCourseId, courseId);
        sql.andEqualTo(CourseExercisesQuestion::getCourseType, courseType);

        Example example = Example.builder(CourseExercisesQuestion.class)
                .where(sql)
                .orderByAsc("sort")
                .build();
        List<CourseExercisesQuestion> list = courseExercisesService.selectByExample(example);
        return list;
    }

    /**
     * 清除某个试题的关联信息
     */
    @DeleteMapping(value = "/{courseType}/{courseId}")
    public Object delete(
            @PathVariable("courseId") Long courseId,
            @PathVariable("courseType") Integer courseType,
            @RequestParam("questionId") Long questionId
    ) {
        courseExercisesService.deleteQuestion(courseId, courseType, questionId);

        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseType, courseId);

        return SuccessMessage.create();
    }

    /**
     * 重新排序
     */
    @PostMapping(value = "changeSort/{courseType}/{courseId}")
    public Object changeSort(
            @PathVariable("courseId") Long courseId,
            @PathVariable("courseType") Integer courseType,
            @RequestBody List<CourseExercisesQuestion> list
    ) {
        courseExercisesService.changeSort(courseId, courseType, list);
        return SuccessMessage.create();
    }

    /**
     * 根据课件ID清除其绑定的课后练习
     * 用于蓝色后台切换科目时候需要切换科目
     *
     */
    @DeleteMapping(value = "changeSubject/{courseWareId}/{courseType}")
    public Object changeSubjectCleanData(@PathVariable("courseWareId") Long courseWareId,
                                         @PathVariable("courseType") Integer courseType) {

        courseExercisesService.deleteQuestionByCourseId(courseWareId, courseType);
        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseType, courseWareId);
        return SuccessMessage.create();
    }




}
