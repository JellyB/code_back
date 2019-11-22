package com.huatu.tiku.course.web.controller.v3;

import com.google.common.collect.Maps;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.common.utils.reflect.ClassUtils;
import com.huatu.tiku.course.bean.HighEndCsFormDTO;
import com.huatu.tiku.course.bean.One2OneFormDTO;
import com.huatu.tiku.course.consts.NetschoolTerminalType;
import com.huatu.tiku.course.netschool.api.v3.UserCoursesServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/14 9:23
 */
@RestController
@RequestMapping("/v3/my")
public class UserCourseControllerV3 {

    @Autowired
    private UserCoursesServiceV3 userCoursesServiceV3;


    /**
     * 我的课程列表
     * @param userSession
     * @param type
     * @param page
     * @return
     */
    @GetMapping("/courses")
    public Object findUserCourses(@Token UserSession userSession,
                                  @RequestParam(required = false)String keywords,
                                  @RequestParam int type,
                                  @RequestParam int page) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("username",userSession.getUname());
        params.put("keywords",keywords);
        params.put("page",page);
        params.put("type",type);
        return ResponseUtil.build(userCoursesServiceV3.findUserCourses(params));
    }

     /**
     * 我的课程列表(隐藏课程列表)
     * @param userSession
     * @param type
     * @param page
     * @return
     */
    @GetMapping( value = "/courses",params = {"_hide"})
    public Object findUserCoursesHiding(@RequestParam int type,
                                        @Token UserSession userSession,
                                        @RequestParam int page) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("username",userSession.getUname());
        params.put("page",page);
        params.put("type",type);

        return ResponseUtil.build(userCoursesServiceV3.findUserHideCourses(params));
    }



    /**
     * 我的直播课程日历
     * @param userSession
     * @return
     */
    @GetMapping("/calendar")
    public Object getLiveCalendar(@Token UserSession userSession) {
        return ResponseUtil.build(userCoursesServiceV3.getLiveCalendar(userSession.getUname()));
    }

    /**
     * 日历直播详情
     * @param id 逗号隔开的
     * @param userSession
     * @return
     */
    @GetMapping("/calendar/show")
    public Object getCalendarDetail(@RequestParam String id,
                                    @Token UserSession userSession) {
        return ResponseUtil.build(userCoursesServiceV3.getCalendarDetail(id));
    }


    /**
     * 填写1对1报名表
     * @param dto
     * @param userSession
     * @param courseId
     * @return
     */
    @PostMapping(value = "/1v1/{courseId}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object save1V1Table(@RequestBody One2OneFormDTO dto,
                               @Token UserSession userSession,
                               @PathVariable int courseId) {
        if(StringUtils.isNotBlank(dto.getViewRate())){
            dto.setViewRatio(dto.getViewRate());//适配之前字段错误的问题
        }
        Map<String,Object> params = ClassUtils.getBeanProperties(dto);
        params.put("action","saveInfo");
        params.put("username",userSession.getUname());
        params.put("rid",courseId);
        return ResponseUtil.build(userCoursesServiceV3.save1V1Table(RequestUtil.encryptParams(params)));
    }


    /**
     * 获取1v1报名表
     * @param courseId
     * @param OrderNum
     * @return
     */
    @GetMapping("/1v1/{courseId}")
    public Object get1V1Table(@PathVariable int courseId,
                              @RequestParam String OrderNum) {
        Map<String,Object> params = HashMapBuilder.newBuilder()
                .put("OrderNum", OrderNum)
                .put("action","getInfo")
                .put("rid",courseId)
                .buildUnsafe();
        return ResponseUtil.build(userCoursesServiceV3.get1V1Table(RequestUtil.encrypt(params)));
    }


    @GetMapping("/highend")
    public Object getHighEndInfo(@Token UserSession userSession){
        Map<String,Object> params = HashMapBuilder.newBuilder()
                .put("username",userSession.getUname())
                .put("action","getinfo")
                .buildUnsafe();
        return ResponseUtil.build(userCoursesServiceV3.getHighEndInfo(RequestUtil.encrypt(params)));
    }

    @PostMapping("/highend")
    public Object setHighEndInfo(@RequestBody HighEndCsFormDTO highEndCsFormDTO,
                                 @Token UserSession userSession){
        Map<String,Object> params = ClassUtils.getBeanProperties(highEndCsFormDTO);
        params.put("action","saveinfo");
        params.put("username",userSession.getUname());
        return ResponseUtil.build(userCoursesServiceV3.setHighEndInfo(RequestUtil.encryptParams(params)),true);
    }

    @PostMapping("/highend/receive")
    public Object receiveHighEndCourse(@RequestHeader int terminal,
                                 @Token UserSession userSession){
        int netschoolTerminal = NetschoolTerminalType.transform(terminal);
        Map<String,Object> params = HashMapBuilder.newBuilder()
                .put("username",userSession.getUname())
                .put("source",netschoolTerminal)
                .buildUnsafe();
        return ResponseUtil.build(userCoursesServiceV3.highendRecieve(RequestUtil.encrypt(params)));
    }

}
