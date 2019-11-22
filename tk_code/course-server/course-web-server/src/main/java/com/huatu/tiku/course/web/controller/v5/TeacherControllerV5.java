package com.huatu.tiku.course.web.controller.v5;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.netschool.api.v5.TeacherServiceV5;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by lijun on 2018/6/26
 */
@RestController
@RequestMapping("/teacher")
@ApiVersion("v5")
public class TeacherControllerV5 {

    @Autowired
    private TeacherServiceV5 teacherService;

    /**
     * 老师在售课程
     */
    @LocalMapParam
    @GetMapping("getPayClassesAll")
    public Object getPayClassesAll(
            @RequestParam String teacherName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(teacherService.getPayClassesAll(map));
    }

    /**
     * 单个老师详情介绍
     */
    @GetMapping("{teacherId}/getTeacherDetail")
    public Object getTeacherDetail(@PathVariable int teacherId) {
        return ResponseUtil.build(teacherService.getTeacherDetail(teacherId));
    }

    /**
     * 老师详情页 历史课件列表
     */
    @LocalMapParam
    @GetMapping("{teacherId}/historyLessonList")
    public Object historyLessonList(
            @PathVariable int teacherId,
                @RequestParam int classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(teacherService.historyLessonList(map));
    }

    /**
     * 老师详情页评价历史课程列表
     */
    @LocalMapParam
    @GetMapping("{teacherId}/historyCourse")
    public Object historyCourse(
            @PathVariable int teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(teacherService.historyCourse(map));
    }

    /**
     * 老师详情页评价课件评价列表
     */
    @LocalMapParam
    @GetMapping("{teacherId}/lessonEvaluateList")
    public Object lessonEvaluateList(
            @PathVariable int teacherId,
            @RequestParam int classId,
            @RequestParam int lessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(teacherService.lessonEvaluateList(map));
    }

}
