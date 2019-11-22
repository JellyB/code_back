package com.huatu.tiku.course.web.controller.v7;

import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v7.SyllabusServiceV7;
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
@ApiVersion("v7")
public class SyllabusControllerV7 {


    @Autowired
    private SyllabusServiceV7 syllabusService;

    @Autowired
    private CourseUtil courseUtil;

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
        map.put("cv", cv);
        StopWatch stopwatch = new StopWatch("app 端售后大纲请求时间统计, classID=" + netClassId);
        stopwatch.start("buyAfterSyllabus.7");
        Object response = ResponseUtil.build(syllabusService.buyAfterSyllabus(map));
        stopwatch.stop();
        //添加答题信息
        stopwatch.start("addExercisesCardInfo");
        courseUtil.addExercisesCardInfoWithCivil((LinkedHashMap) response, userSession.getId(), false);
        courseUtil.addExercisesCardInfoWithEssay((LinkedHashMap) response, userSession.getId());
        stopwatch.stop();
        stopwatch.start("buyAfterSyllabus.7 - addPeriodTestInfo 1");
        courseUtil.addPeriodTestInfo((LinkedHashMap) response, userSession.getId());
        stopwatch.stop();
        stopwatch.start("buyAfterSyllabus.7 - addLearnReportInfoV2 2");
        courseUtil.addLearnReportInfoV2((LinkedHashMap) response, userSession.getId());
        stopwatch.stop();
        stopwatch.start("buyAfterSyllabus.7 - addLiveCardExercisesCardInfo 3");
        courseUtil.addLiveCardExercisesCardInfo((LinkedHashMap) response, userSession.getId(), false);
        stopwatch.stop();
        log.info("buyAfterSyllabus.7 - 阶段测试-学习报告-课后练习状态统计耗时汇总:{}", stopwatch.prettyPrint());
        //添加答题信息
        log.info("app 端请求课后作业超时时间统计.7, classId:{}耗时:{}", netClassId, stopwatch.prettyPrint());
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
        return timeTable;
    }

    /**
     *
     * 分享音频课件列表
     *
     * @param terminal
     * @param cv
     * @param netClassId
     * @param shareSyllabusId
     * @param afterNodeId
     * @param beforeNodeId
     * @return
     */
    @GetMapping("{netClassId}/shareAudio")
    public Object shareAudio(@RequestHeader(value = "terminal") int terminal,
                                   @RequestHeader(value = "cv") String cv,
                                   @PathVariable int netClassId,
                                   @RequestParam(value = "shareSyllabusId") int shareSyllabusId,
                                   @RequestParam(defaultValue = "0") int afterNodeId,
                                   @RequestParam(defaultValue = "0") int beforeNodeId){
        Map<String,Object> params = Maps.newHashMap();
        params.put("terminal", terminal);
        params.put("cv", cv);
        params.put("netClassId", netClassId);
        params.put("shareSyllabusId", shareSyllabusId);
        params.put("afterNodeId", afterNodeId);
        params.put("beforeNodeId", beforeNodeId);
        return ResponseUtil.build(syllabusService.shareAudio(params));
    }
}
