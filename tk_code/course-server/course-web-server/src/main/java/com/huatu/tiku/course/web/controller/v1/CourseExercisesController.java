package com.huatu.tiku.course.web.controller.v1;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.service.v1.CourseExercisesService;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.springboot.users.support.Token;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/19
 */
@RestController
@RequestMapping("exercises")
@ApiVersion("v1")
public class CourseExercisesController {

    @Autowired
    private CourseExercisesService service;

    @Autowired
    private PracticeCardServiceV1 practiceCardService;

    /**
     * 创建课后训练答题卡
     *
     * @return
     */
    @GetMapping(value = "/{courseType}/{courseId}/card")
    public Object card(
            @Token UserSession userSession,
            @RequestHeader("terminal") Integer terminal,
            @PathVariable(value = "courseType") Integer courseType,
            @PathVariable(value = "courseId") Long courseId
    ) {
        List<Map<String, Object>> list = service.listQuestionByCourseId(courseType, courseId);
        if (CollectionUtils.isEmpty(list)) {
            //return ErrorResult.create(5000000, "暂无答题卡信息");
            return null;
        }
        int subjectId = userSession.getSubject();
        String questionId = list.stream()
                .filter(map -> null != map && null != map.get("id"))
                .map(map -> String.valueOf(map.get("id")))
                .collect(Collectors.joining(","));
        Object practiceCard = practiceCardService.createCourseExercisesPracticeCard(
                terminal, subjectId, userSession.getId(), "课后练习",
                courseType, courseId, questionId
        );
        HashMap<String, Object> result = (HashMap<String, Object>) ZTKResponseUtil.build(practiceCard);
        if (null == result) {
            //return ErrorResult.create(5000000, "暂无答题卡信息");
            return null;
        }
        result.computeIfPresent("id", (key, value) -> String.valueOf(value));
        result.computeIfPresent("score", (key, value) -> new Double(Double.parseDouble(value.toString())).intValue());
        return result;
    }
}
