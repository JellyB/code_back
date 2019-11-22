package com.huatu.tiku.course.web.controller.v5;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v5.UserCourseServiceV5;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by lijun on 2018/6/25
 */
@RestController
@RequestMapping("/my")
@ApiVersion("v5")
public class UserCourseControllerV5 {

    @Autowired
    private UserCourseServiceV5 userCourseService;

    /**
     * 获取我的课程-已删除
     */
    @LocalMapParam(checkToken = true)
    @GetMapping("getMyDeletedClasses")
    public Object getMyDeletedClasses(
            @RequestParam(defaultValue = "0") int cateId,
            @RequestParam(defaultValue = "") String keyWord,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(userCourseService.getMyDeletedClasses(map));
    }

    /**
     * 获取我的课程-未删除
     */
    @LocalMapParam(checkToken = true)
    @GetMapping("getMyNotDeletedClasses")
    public Object getMyNotDeletedClasses(
            @RequestParam(defaultValue = "0") int cateId,
            @RequestParam(defaultValue = "") String keyWord,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(userCourseService.getMyNotDeletedClasses(map));
    }

    /**
     * 获取我的直播日历
     */
    @GetMapping("getLiveCalendar")
    public Object getLiveCalendar(@Token UserSession userSession) {
        return ResponseUtil.build(userCourseService.liveCalendar(userSession.getUname()));
    }

    /**
     * 获取我的直播日历详情
     */
    @GetMapping("{idList}/liveCalendarDetail")
    public Object liveCalendar(@PathVariable("idList") String idList) {
        return ResponseUtil.build(userCourseService.liveCalendarDetail(idList));
    }
}
