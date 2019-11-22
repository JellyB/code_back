package com.huatu.tiku.course.web.controller.v1;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.service.v1.CourseBreakpointService;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/19
 */
@RestController
@RequestMapping("breakPoint")
@ApiVersion("v1")
public class CourseBreakPointController {

    @Autowired
    private CourseBreakpointService service;

    /**
     * 创建课中训练答题卡
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
        HashMap<String, Object> map = service.buildCard(terminal, userSession.getSubject(), userSession.getId(), courseType, courseId);
        do {
            if (null == map) {
                break;
            }
            if (null == map.get("paper")) {
                break;
            }
            if (null == ((HashMap<String, Object>) map.get("paper")).get("breakPointInfoList")) {
                break;
            }
            Map<String, List<HashMap>> collect = ((ArrayList<HashMap>) ((HashMap<String, Object>) map.get("paper")).get("breakPointInfoList"))
                    .stream()
                    .collect(Collectors.groupingBy(data -> data.get("position").toString()));
            ((HashMap<String, Object>) map.get("paper")).put("breakPointInfoList", collect);
        } while (false);
        return map;
    }

    /**
     * 创建课中训练答题卡
     *
     * @return
     */
    @GetMapping(value = "/{courseType}/{courseId}/cardForAndroid")
    public Object cardForAndroid(
            @Token UserSession userSession,
            @RequestHeader("terminal") Integer terminal,
            @PathVariable(value = "courseType") Integer courseType,
            @PathVariable(value = "courseId") Long courseId
    ) {
        HashMap<String, Object> map = service.buildCard(terminal, userSession.getSubject(), userSession.getId(), courseType, courseId);
        if (null == map){
            //return ErrorResult.create(5000000, "暂无答题卡信息");
            return null;
        }
        return map;
    }
}
