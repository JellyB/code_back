package com.huatu.tiku.banckend.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.banckend.service.impl.AsyncTaskServiceImpl;
import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;

/**
 * 课程-断点 - 试题关联信息管理
 * Created by lijun on 2018/6/12
 */
@RestController
@RequestMapping("/backend/courseBreakpointQuestion")
public class CourseBreakpointQuestionController {
    @Autowired
    private CourseBreakpointService courseBreakpointService;
    @Autowired
    private CourseBreakpointQuestionService courseBreakpointQuestionService;
    @Autowired
    private AsyncTaskServiceImpl asyncTaskServiceImpl;
    @Autowired
    private CourseKnowledgeService courseKnowledgeService;

    /**
     * 查询某个节点下所有关联的试题信息
     *
     * @return
     */
    @GetMapping(value = "list/{courseBreakpointId}")
    public Object list(@PathVariable("courseBreakpointId") Long courseBreakpointId) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, courseBreakpointId);

        Example example = Example.builder(CourseBreakpointQuestion.class)
                .andWhere(sql)
                .orderByAsc("sort")
                .build();
        List<CourseBreakpointQuestion> list = courseBreakpointQuestionService.selectByExample(example);
        return list;
    }

    /**
     * 编辑关联信息
     *
     * @param courseBreakpointQuestion
     * @return
     */
    @PostMapping(value = "edit")
    public Object edit(@Validated @RequestBody CourseBreakpointQuestion courseBreakpointQuestion) {
        //给定默认的用户ID
        courseBreakpointQuestion.setUserId(1L);
        //默认显示
        courseBreakpointQuestion.setDisplayStem(1);
        courseBreakpointQuestionService.save(courseBreakpointQuestion);

        CourseBreakpoint breakpoint = CourseBreakpoint.builder().id(courseBreakpointQuestion.getBreakpointId()).build();
        CourseBreakpoint courseBreakpoint = courseBreakpointService.selectOne(breakpoint);
        //根据试题更新课件知识点
        courseKnowledgeService.boundKnowledgeByQuestion(courseBreakpoint.getCourseId(),courseBreakpoint.getCourseType(),courseBreakpointQuestion.getQuestionId());

        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseBreakpoint.getCourseType(), courseBreakpoint.getCourseId());

        return SuccessMessage.create();
    }


    /**
     * 删除某个断点-用户消息
     */
    @DeleteMapping(value = "/{pointId}")
    public Object delete(
            @PathVariable(value = "pointId") Long pointId,
            @RequestParam(value = "questionId") Long questionId
    ) {
        WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, pointId);
        sql.andEqualTo(CourseBreakpointQuestion::getQuestionId, questionId);
        Example example = Example.builder(CourseBreakpointQuestion.class)
                .where(sql)
                .build();
        courseBreakpointQuestionService.deleteByExample(example);

        //异步，更新php端练习数量
        CourseBreakpoint breakpoint = CourseBreakpoint.builder().id(pointId).build();
        CourseBreakpoint courseBreakpoint = courseBreakpointService.selectOne(breakpoint);
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseBreakpoint.getCourseType(), courseBreakpoint.getCourseId());
        return SuccessMessage.create();
    }

    /**
     * 试题排序
     */
    @PostMapping(value = "changeSort/{courseBreakpointId}")
    public Object changeSort(
            @PathVariable("courseBreakpointId") Long courseBreakpointId,
            @RequestBody List<CourseBreakpointQuestion> list
    ) {
        courseBreakpointQuestionService.changeSort(courseBreakpointId, list);
        return SuccessMessage.create();
    }
}
