package com.huatu.tiku.course.web.controller.v6;

import com.huatu.common.exception.BizException;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.SyllabusServiceV6;
import com.huatu.tiku.course.service.v1.VersionControlService;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.web.controller.util.CourseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-03 下午4:11
 **/

@Slf4j
@RestController
@RequestMapping("/syllabus")
@ApiVersion("v6")
public class SyllabusControllerV6 {


    @Autowired
    private SyllabusServiceV6 syllabusService;

    @Autowired
    private CourseUtil courseUtil;

    @Autowired
    private VersionControlService versionControlService;

    /**
     * 课程大纲课程列表 7.1.1
     * @param cv
     * @param terminal
     * @param netClassId
     * @param stageNodeId
     * @return
     */
    @LocalMapParam
    @GetMapping("/{netClassId}/syllabusClasses")
    public Object syllabusClasses(@RequestHeader(value = "cv") String cv,
                                  @RequestHeader(value = "terminal") int terminal,
                                  @PathVariable(value = "netClassId") int netClassId,
                                  @RequestParam(defaultValue = "0") int stageNodeId) {


        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse =  syllabusService.syllabusClasses(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 售后大纲老师列表（7.1.1）
     */
    @LocalMapParam
    @GetMapping("{netClassId}/syllabusTeachers")
    public Object syllabusTeachers(@RequestHeader(value = "terminal") int terminal,
                                   @PathVariable(value = "netClassId") int netClassId,
                                   @RequestParam(defaultValue = "0") int classNodeId,
                                   @RequestParam(defaultValue = "0") int stageNodeId) throws BizException{
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(syllabusService.syllabusTeachers(map));
    }


    /**
     * 大纲 售后
     */
    @LocalMapParam(checkToken = true)
    @GetMapping("{netClassId}/buyAfterSyllabus")
    public Object buyAfterSyllabus(
            @Token UserSession userSession,
            @RequestHeader(value = "terminal") int terminal,
            @RequestHeader(value = "cv") String cv,
            @RequestParam(defaultValue = "") String teacherId,
            @RequestParam(defaultValue = "0") int stageNodeId,
            @RequestParam(defaultValue = "1") int position,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int nextClassNodeId,
            @RequestParam(defaultValue = "0") int nextCoursewareNodeId,
            @PathVariable int netClassId,
            @RequestParam(defaultValue = "0") int coursewareNodeId,
            @RequestParam(defaultValue = "") String classNodeId,
            @RequestParam(defaultValue = "0") int afterNodeId,
            @RequestParam(defaultValue = "0") int beforeNodeId,
            @RequestParam(defaultValue = "0", required = false) int parentNodeId
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        StopWatch stopwatch = new StopWatch("app 端售后大纲请求时间统计");
        stopwatch.start("buyAfterSyllabus");
        Object response = ResponseUtil.build(syllabusService.buyAfterSyllabus(map));
        stopwatch.stop();
        //添加答题信息
        stopwatch.start("addExercisesCardInfo");
        courseUtil.filterV5AndV6((LinkedHashMap) response);
        courseUtil.addExercisesCardInfoV3((LinkedHashMap) response, userSession.getId(), false);
        stopwatch.stop();
        if(versionControlService.checkLearnReportShow(terminal, cv)){
            stopwatch.start("buyAfterSyllabus - addPeriodTestInfo 1");
            courseUtil.addPeriodTestInfo((LinkedHashMap) response, userSession.getId());
            stopwatch.stop();
            stopwatch.start("buyAfterSyllabus - addLearnReportInfoV2 2");
            courseUtil.addLearnReportInfoV2((LinkedHashMap) response, userSession.getId());
            stopwatch.stop();
            stopwatch.start("buyAfterSyllabus - addLiveCardExercisesCardInfo 3");
            courseUtil.addLiveCardExercisesCardInfo((LinkedHashMap) response, userSession.getId(), false);
            stopwatch.stop();
            log.info("buyAfterSyllabus - 阶段测试-学习报告-课后练习状态统计耗时汇总:{}", stopwatch.prettyPrint());
        }

        //添加答题信息
        log.info("app 端请求课后作业超时时间统计, 耗时:{}", stopwatch.prettyPrint());
        return response;
    }

    /**
     * 获取课程大纲-售前
     */
    @LocalMapParam
    @GetMapping("/{classId}/classSyllabus")
    public Object classSyllabus(
            @Token UserSession userSession,
            @RequestHeader(value = "terminal") int terminal,
            @PathVariable("classId") int classId,
            @RequestParam int parentId,
            @RequestParam(defaultValue = "0") int onlyTrial,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) throws BizException {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        //添加答题信息
        Object timeTable = ResponseUtil.build(syllabusService.classSyllabus(map));
        //添加答题信息
        courseUtil.addExercisesCardInfoV3((LinkedHashMap) timeTable, userSession.getId(), false);
        return timeTable;
    }



    /**
     * 获取课程大纲-售前-不用登录
     */
    @LocalMapParam
    @GetMapping("/{classId}/classSyllabusWithoutSession")
    public Object classSyllabusWithoutSession(
            @RequestHeader(value = "terminal") int terminal,
            @PathVariable("classId") int classId,
            @RequestParam int parentId,
            @RequestParam(defaultValue = "0") int onlyTrial,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(syllabusService.classSyllabus(map));
    }
}
