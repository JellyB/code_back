package com.huatu.tiku.course.web.controller.v4;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v4.AppServiceV4;
import com.huatu.tiku.course.service.v4.CourseBizServiceV4;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by lijun on 2018/5/18
 */
@Slf4j
@RestController
@RequestMapping("/v4/evaluates")
public class EvaluateControllerV4 {

    @Autowired
    private AppServiceV4 appService;

    @Autowired
    private CourseBizServiceV4 courseBizServiceV4;

    /**
     * 课件是否被用户评价
     *
     * @return
     */
    @LocalMapParam
    @GetMapping("/lession")
    public Object lessionEvaluate(
            @Token UserSession userSession,
            @RequestParam("lessionId") int lessionId,
            @RequestParam(defaultValue = "0") int syllabusId
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = appService.lessionEvaluate(map);
        return ResponseUtil.build(netSchoolResponse);
    }


    @GetMapping("/token")
    public Object lessionToken(
            @RequestParam(required = false) String bjyRoomId,
            @RequestParam(required = false) String bjySessionId,
            @RequestParam(required = false) String videoId) {
        if (StringUtils.isBlank(bjyRoomId) && StringUtils.isBlank(videoId)) {
            throw new BizException(ErrorResult.create(50000, "服务器内部错误,缺少参数"));
        }
        NetSchoolResponse netSchoolResponse = appService.lessionToken(bjyRoomId, bjySessionId, videoId);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 查询合集列表
     */
    @LocalMapParam
    @GetMapping("/collectionClasses")
    public Object collectionClasses(
            @RequestHeader(value = "terminal", required = false) Integer terminal,
            @RequestHeader(value = "cv", required = false) String cv,
            @Token UserSession userSession,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam int collectionId
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        //行为日志收集   格式说明 在云盘上 http://123.103.79.72:8025/index.php?explorer
        log.warn("3$${}$${}$${}$${}$${}$${}", collectionId, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        return courseBizServiceV4.collectionClasses(map);
    }

    /**
     * 专栏
     */
    @GetMapping("/specialColumn")
    public Object specialColumn() {
        return ResponseUtil.build(appService.specialColumn());
    }
}
