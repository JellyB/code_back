package com.huatu.tiku.course.web.controller.v6;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.EvaluationServiceV6;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 描述：我的评价接口
 *
 * @author biguodong
 * Create time 2018-11-30 上午11:42
 **/

@Slf4j
@RestController
@RequestMapping("evaluation")
@ApiVersion("v6")
public class EvaluationControllerV6 {

    @Autowired
    private EvaluationServiceV6 evaluationService;


    /**
     * 获取售前课程详情评价列表
     */
    @LocalMapParam
    @GetMapping("getClassEvaluation")
    public Object getClassEvaluation(
            @Token UserSession userSession,
            @RequestHeader(value = "terminal") int terminal,
            @RequestParam(value = "classId")int classId,
            @RequestParam(value = "isLive", defaultValue = "0") int isLive,
            @RequestParam(value = "mine") int mine,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = evaluationService.getClassEvaluation(params);
        params.put("mine", mine);
        return ResponseUtil.build(netSchoolResponse);
    }
}
