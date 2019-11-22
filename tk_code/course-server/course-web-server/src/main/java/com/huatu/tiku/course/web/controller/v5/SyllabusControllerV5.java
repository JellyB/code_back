package com.huatu.tiku.course.web.controller.v5;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v5.SyllabusServiceV5;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.web.controller.util.CourseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 课程大纲
 * Created by lijun on 2018/11/7
 */
@Slf4j
@RestController
@RequestMapping("/syllabus")
@ApiVersion("v5")
public class SyllabusControllerV5 {

    @Autowired
    private SyllabusServiceV5 syllabusService;

    @Autowired
    private CourseUtil courseUtil;

    /**
     * 大纲 售后
     */
    @LocalMapParam(checkToken = true)
    @GetMapping("{netClassId}/buyAfterSyllabus")
    public Object buyAfterSyllabus(
            @Token UserSession userSession,
            @PathVariable int netClassId,
            @RequestParam(defaultValue = "") String classId,
            @RequestParam(defaultValue = "") String classNodeId,
            @RequestParam(defaultValue = "") String teacherId,
            @RequestParam(defaultValue = "0") int coursewareNodeId,
            @RequestParam(defaultValue = "1") int position,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        Object response = ResponseUtil.build(syllabusService.buyAfterSyllabus(map));
        courseUtil.filterV5AndV6((LinkedHashMap) response);
        //添加答题信息
        courseUtil.addExercisesCardInfoV3((LinkedHashMap) response, userSession.getId(), false);
        return response;
    }

    /**
     * 售后大纲课程列表（7.1.1）
     */
    @LocalMapParam
    @GetMapping("{netClassId}/syllabusClasses")
    public Object syllabusClasses(@PathVariable int netClassId) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(syllabusService.syllabusClasses(map));
    }

    /**
     * 售后大纲老师列表（7.1.1）
     */
    @LocalMapParam
    @GetMapping("{netClassId}/syllabusTeachers")
    public Object syllabusTeachers(@PathVariable int netClassId) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(syllabusService.syllabusTeachers(map));
    }
}
