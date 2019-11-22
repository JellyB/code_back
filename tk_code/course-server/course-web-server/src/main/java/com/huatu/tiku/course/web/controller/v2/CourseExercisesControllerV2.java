package com.huatu.tiku.course.web.controller.v2;

import com.google.common.collect.Maps;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.manager.CourseExercisesStatisticsManager;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 描述：创建 & 没有听过直播的直播回放 创建课后作业答题卡逻辑
 *
 * @author biguodong
 * Create time 2019-03-08 5:14 PM
 **/


@RestController
@RequestMapping("exercises")
@ApiVersion("v2")
@Slf4j
public class CourseExercisesControllerV2 {



    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;


    @Autowired
    private CourseExercisesStatisticsManager courseExercisesStatisticsManager;
    /**
     * 创建课后训练答题卡
     *
     * @return
     */
    @GetMapping(value = "/{courseType}/{coursewareId}/card")
    public Object card(
            @Token UserSession userSession,
            @RequestHeader("terminal") Integer terminal,
            @RequestHeader(value = "cv", required = false, defaultValue = "1.0") String cv,
            @PathVariable(value = "courseType") Integer courseType,
            @PathVariable(value = "coursewareId") Long coursewareId,
            @RequestParam(value = "courseId") Long courseId,
            @RequestParam(value = "syllabusId") Long syllabusId

    ) {
        try{
            return courseExercisesProcessLogManager.createCourseWorkAnswerCardEntranceV2(courseId, syllabusId, courseType, coursewareId, userSession.getSubject(), terminal, cv, userSession.getId());
        }catch (Exception e){
            log.error("CourseExercisesControllerV2 card caught an exception :params courseId:{}, syllabusId:{}, courseType:{}, coursewareId:{}, subject:{}, terminal:{}, cv:{}, id:{}, error:{}", courseId, syllabusId, courseType, coursewareId, userSession.getSubject(), terminal, cv, userSession.getId(), e.getMessage());
            e.printStackTrace();
            return Maps.newLinkedHashMap();
        }
    }

    /**
     * 查询答题卡状态 0 未完成 1 已完成
     * @param userSession
     * @return
     */
    @GetMapping("card/info")
    public Object cardInfo(@Token UserSession userSession,
                           @RequestParam(value = "syllabusId") long syllabusId){
        return courseExercisesProcessLogManager.cardInfo(userSession.getId(), syllabusId);
    }




    /**
     * 课后作业统计信息
     * @param params
     * @return
     */
    @PostMapping(value = "/statistics")
    public Object statistics(@RequestBody List<Map<String,Object>> params){
        return courseExercisesStatisticsManager.statistics(params);
    }

    /**
     * 每次课后作业的详细统计信息
     * @param id
     * @return
     */
    @GetMapping(value = "/statistics/{id}")
    public Object statisticsDetail(@PathVariable(value = "id") long id){
        return courseExercisesStatisticsManager.statisticsDetail(id);
    }
}

