package com.huatu.tiku.banckend.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.banckend.service.CourseBreakpointQuestionService;
import com.huatu.tiku.banckend.service.CourseBreakpointService;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.banckend.service.impl.AsyncTaskServiceImpl;
import com.huatu.tiku.dto.request.BreakPointQuestionVO;
import com.huatu.tiku.entity.CourseBreakpoint;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.util.log.LogPrint;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程-直播随堂练习 - 试题关联信息管理
 * Created by zhaoxi on 2018/6/12
 */
@RestController
@RequestMapping("/backend/live")
public class LiveQuestionController {

    @Autowired
    private CourseBreakpointService courseBreakpointService;
    @Autowired
    private CourseBreakpointQuestionService courseBreakpointQuestionService;
    @Autowired
    private AsyncTaskServiceImpl asyncTaskServiceImpl;
    @Autowired
    private CourseKnowledgeService courseKnowledgeService;
    /**
     * 编辑关联信息
     *
     * @param liveQuestion
     * @return
     */
    @LogPrint
    @PostMapping(value = "edit")
    public Object edit(@Validated @RequestBody BreakPointQuestionVO liveQuestion) {

        Integer courseType = liveQuestion.getCourseType();
        Long courseId = liveQuestion.getCourseId();

        List<CourseBreakpoint> breakpointList = courseBreakpointService.listData(courseType, courseId, "", 0);
        long breakPointId;
        if (CollectionUtils.isEmpty(breakpointList)) {
            CourseBreakpoint breakpoint = CourseBreakpoint.builder()
                    .position(-1)
                    .sort(1)
                    .pointName("直播随堂练习")
                    .courseType(courseType)
                    .courseId(courseId)
                    .creatorId(1L)
                    .build();
            Integer save = courseBreakpointService.save(breakpoint);
            breakPointId = breakpoint.getId();
        } else {
            breakPointId = breakpointList.get(0).getId();
        }

        CourseBreakpointQuestion courseBreakpointQuestion = new CourseBreakpointQuestion();
        BeanUtils.copyProperties(liveQuestion, courseBreakpointQuestion);
        courseBreakpointQuestion.setBreakpointId(breakPointId);
        //给定默认的用户ID
        liveQuestion.setUserId(1L);
        //默认显示
        liveQuestion.setDisplayStem(1);
        if(null == courseBreakpointQuestion.getPptIndex() || 0 <= courseBreakpointQuestion.getPptIndex()){
            courseBreakpointQuestion.setPptIndex(9999);
        }
        courseBreakpointQuestionService.save(courseBreakpointQuestion);

        //根据试题更新课件知识点
        courseKnowledgeService.boundKnowledgeByQuestion(courseId,courseType,liveQuestion.getQuestionId());


        //异步，更新php端练习数量
        asyncTaskServiceImpl.upQuestionNumOfCourse(courseType, courseId);

        return SuccessMessage.create();
    }


    /**
     * 查询直播随堂练习试题信息
     */
    @LogPrint
    @GetMapping(value = "/{courseType}/{courseId}")
    public Object list(@PathVariable("courseId") Long courseId,
                       @PathVariable("courseType") Integer courseType) {
        List<CourseBreakpoint> breakpointList = courseBreakpointService.listData(courseType, courseId, "", 0);

        if (CollectionUtils.isNotEmpty(breakpointList)) {
            WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
            sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, breakpointList.get(0).getId());

            Example example = Example.builder(CourseBreakpointQuestion.class)
                    .andWhere(sql)
                    .orderByAsc("pptIndex","sort")
                    .build();
            List<CourseBreakpointQuestion> list = courseBreakpointQuestionService.selectByExample(example);
            list.forEach(courseBreakpointQuestion -> {
                if(courseBreakpointQuestion.getPptIndex() != null && courseBreakpointQuestion.getPptIndex().equals(9999)){
                    courseBreakpointQuestion.setPptIndex(null);
                }
            });
            return list;
        }
        return new ArrayList<>();
    }


    /**
     * 更新 直播随堂练习 PPT 下标
     */
    @LogPrint
    @PostMapping(value = "{id}/editPPTIndex")
    public Object editPPTIndex(@PathVariable Long id, @RequestParam Integer pptIndex) {
        final CourseBreakpointQuestion courseBreakpointQuestion = courseBreakpointQuestionService.selectByPrimaryKey(id);
        if (null != courseBreakpointQuestion) {
            courseBreakpointQuestion.setPptIndex(pptIndex);
            courseBreakpointQuestionService.updateByPrimaryKey(courseBreakpointQuestion);
        }
        return SuccessMessage.create();
    }


    /**
     * 更新直播随堂练习试题排序
     */
    @PostMapping(value = "changeSort/{courseType}/{courseId}")
    public Object changeSort(
            @PathVariable("courseId") Long courseId,
            @PathVariable("courseType") Integer courseType,
            @RequestBody List<CourseBreakpointQuestion> list) {
        List<CourseBreakpoint> breakpointList = courseBreakpointService.listData(courseType, courseId, "", 0);
        if (CollectionUtils.isNotEmpty(breakpointList)) {
            courseBreakpointQuestionService.changeSort(breakpointList.get(0).getId(), list);
        }
        return SuccessMessage.create();
    }


    /**
     * 删除某个直播随堂练习
     */
    @DeleteMapping(value = "/{courseType}/{courseId}")
    public Object delete(
            @PathVariable("courseId") Long courseId,
            @PathVariable("courseType") Integer courseType,
            @RequestParam(value = "questionId") Long questionId
    ) {
        long pointId = 0;
        List<CourseBreakpoint> breakpointList = courseBreakpointService.listData(courseType, courseId, "", 0);
        if (CollectionUtils.isNotEmpty(breakpointList)) {
            pointId = breakpointList.get(0).getId();
            WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
            sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, pointId);
            sql.andEqualTo(CourseBreakpointQuestion::getQuestionId, questionId);
            Example example = Example.builder(CourseBreakpointQuestion.class)
                    .where(sql)
                    .build();
            courseBreakpointQuestionService.deleteByExample(example);

            //异步，更新php端练习数量
            asyncTaskServiceImpl.upQuestionNumOfCourse(courseType, courseId);
        }


        return SuccessMessage.create();
    }

}
