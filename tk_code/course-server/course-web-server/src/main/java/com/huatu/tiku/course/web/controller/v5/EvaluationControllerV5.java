package com.huatu.tiku.course.web.controller.v5;

import com.huatu.common.spring.event.EventPublisher;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v5.EvaluationServiceV5;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by lijun on 2018/6/25
 */
@Slf4j
@RestController
@RequestMapping("evaluation")
@ApiVersion("v5")
public class EvaluationControllerV5 {

    @Autowired
    private EvaluationServiceV5 evaluationService;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * 获取售前课程详情评价列表
     */
    @GetMapping("getClassEvaluation")
    public Object getClassEvaluation(
            @RequestHeader int terminal,
            @RequestParam int classId,
            @RequestParam(defaultValue = "0") int isLive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
                .put("classId", classId)
                .put("terminal", terminal)
                .put("isLive", isLive)
                .put("page", page)
                .put("pageSize", pageSize)
                .build();
        return ResponseUtil.build(evaluationService.getClassEvaluation(map));
    }

    /**
     * 获取老师介绍页评价列表
     */
    @GetMapping("getTeacherEvaluation")
    public Object getTeacherEvaluation(
            @RequestHeader int terminal,
            @RequestParam int teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
                .put("terminal", terminal)
                .put("teacherId", teacherId)
                .put("page", page)
                .put("pageSize", pageSize)
                .build();
        return ResponseUtil.build(evaluationService.getTeacherEvaluation(map));
    }

    /**
     * 获取用户当前课件评价信息接口
     */
    @LocalMapParam(checkToken = true)
    @GetMapping("getClickEvaluation")
    public Object getClickEvaluation(
            @Token UserSession userSession,
            @RequestParam int classId,
            @RequestParam int lessonId,
            @RequestParam int parentId
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(evaluationService.getClickEvaluation(map));
    }

    /**
     * 提交评价
     */
    @LocalMapParam(checkToken = true)
    @PostMapping("submit")
    public Object submit(
            @Token UserSession userSession,
            @RequestParam int classId,
            @RequestParam String evaluation,
            @RequestParam int lessonId,
            @RequestParam String score,
            @RequestParam int parentId
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        NetSchoolResponse response = evaluationService.submit(map);
        if (ResponseUtil.isSuccess(response)) {
            eventPublisher.publishEvent(RewardActionEvent.class,
                    this,
                    (event) -> event.setAction(RewardAction.ActionType.EVALUATE_AFTER)
                            .setUid(userSession.getId())
                            .setUname(userSession.getUname())
            );
        }
        return ResponseUtil.build(response);
    }
}
