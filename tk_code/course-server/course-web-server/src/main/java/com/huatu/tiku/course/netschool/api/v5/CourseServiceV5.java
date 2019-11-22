package com.huatu.tiku.course.netschool.api.v5;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.Result;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v5.configuration.CourseServiceV5Config;
import com.huatu.tiku.course.util.ResponseUtil;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程相关
 * Created by lijun on 2018/6/25
 */
@FeignClient(value = "o-course-service", path = "/lumenapi", configuration = CourseServiceV5Config.class, fallbackFactory = CourseServiceV5.CourseServiceV5FallbackFactory.class)
public interface CourseServiceV5 {

    /**
     * 获取录播课程合集
     */
    @GetMapping(value = "/v4/common/class/class_list?videoType=0")
    NetSchoolResponse recordClassList(@RequestParam Map<String, Object> params);

    /**
     * 获取直播课程合集
     */
    @GetMapping(value = "/v4/common/class/class_list?videoType=1")
    NetSchoolResponse liveClassList(@RequestParam Map<String, Object> params);

    /**
     * 获取面库课程合集
     */
    @GetMapping(value = "/v4/common/class/class_list?videoType=4")
    NetSchoolResponse icClassList(@RequestParam Map<String, Object> params);

    /**
     * 课程播放接口
     */
    @GetMapping(value = "/v4/common/user/play")
    NetSchoolResponse getCommonUserPlay(@RequestParam Map<String, Object> params);

    /**
     * 课程大纲-售前
     * TODO: v3 Service 中有降级代码，但未被启用。
     */
    @GetMapping(value = "/v4/common/class/class_syllabus")
    NetSchoolResponse findTimetable(@RequestParam Map<String, Object> params);

    /**
     * 课程大纲-售后
     */
    @GetMapping(value = "/v4/common/class/purchased_class_syllabus")
    NetSchoolResponse findPurchasesTimetable(@RequestParam Map<String, Object> params);

    /**
     * 查询课程详情 - 录播
     * 该接口在V5 之前有大量的业务处理。
     */
    @GetMapping(value = "/v4/common/class/class_detail?isLive=0")
    NetSchoolResponse getClassDetailNotLive(@RequestParam Map<String, Object> params);

    /**
     * 查询课程详情 - 直播
     * 该接口在V5 之前有大量的业务处理。
     */
    @GetMapping(value = "/v4/common/class/class_detail?isLive=1")
    NetSchoolResponse getClassDetailLive(@RequestParam Map<String, Object> params);

    /**
     * 获取课程介绍
     */
    @GetMapping(value = "/v4/common/class/course_introduction")
    NetSchoolResponse getCourseIntroduction(@RequestParam Map<String, Object> params);

    /**
     * 获取所有老师介绍
     */
    @GetMapping(value = "/v4/common/teacher/teacher_info")
    NetSchoolResponse getCourseTeacherInfo(@RequestParam("rid") int teacherId);

    /**
     * 获取课程说明
     * 获取 html 页面
     */
    @GetMapping(value = "/v4/common/class/class_ext")
    String getClassExt(@RequestParam("classId") int classId, @RequestParam("terminal") int terminal);

    /**
     * 删除课程
     */
    @DeleteMapping(value = "/v4/common/user/my_course")
    NetSchoolResponse deleteCourse(@RequestParam Map<String, Object> params);

    /**
     * 彻底删除回收站课程
     */
    @DeleteMapping(value = "/v4/common/user/my_course_deep")
    NetSchoolResponse deleteDeepCourse(@RequestParam Map<String, Object> params);

    /**
     * 取消删除
     */
    @PutMapping(value = "/v4/common/user/my_course")
    NetSchoolResponse cancelDeleteCourse(@RequestParam Map<String, Object> params);

    /**
     * 置顶课程
     */
    @PostMapping(value = "/v4/common/user/top_course")
    NetSchoolResponse postTopCourse(@RequestParam Map<String, Object> params);

    /**
     * 删除置顶课程
     */
    @DeleteMapping(value = "/v4/common/user/top_course")
    NetSchoolResponse deleteTopCourse(@RequestParam Map<String, Object> params);

    /**
     * qq群、课程学习总进度
     */
    @GetMapping(value = "/v4/common/class/qq_group_schedule")
    NetSchoolResponse qqGroupSchedule(@RequestParam Map<String, Object> params);

    /**
     * 获取最后一次学习课程 - IC
     */
    @GetMapping(value = "/v4/interview/user/last_study")
    NetSchoolResponse lastStudyCourse(@RequestParam Map<String, Object> params);

    /**
     * 已够课程列表 - IC
     */
    @GetMapping(value = "/v4/interview/class/list")
    NetSchoolResponse userCourseList(@RequestParam Map<String, Object> params);

    /**
     * 根据课程id 批量获取数据
     */
    @GetMapping(value = "/v4/interview/class/list_by_id")
    NetSchoolResponse courseInfoList(@RequestParam("classIds") String classIds);

    /**
     * 获取横屏课程信息
     */
    @GetMapping(value = "/v4/common/class/choose_courseware")
    NetSchoolResponse chooseCourseWare(@RequestParam Map<String, Object> params);

    /**
     * 继续学习
     */
    @GetMapping(value = "/v4/common/class/last_play_lesson")
    NetSchoolResponse lastPlayLesson(@RequestParam Map<String, Object> params);

    /**
     * 讲义
     */
    @GetMapping(value = "/v4/common/class/get_handouts")
    NetSchoolResponse handouts(@RequestParam Map<String, Object> params);

    /**
     * 课程详情活动促销
     */
    @GetMapping(value = "/v4/common/class/appClass_activity_details")
    NetSchoolResponse appClassActivityDetails(@RequestParam("classId") int classIds,
                                              @RequestParam("terminal") int terminal);

    /**
     * 试听列表
     */
    @GetMapping(value = "/v4/common/class/class_audition_list")
    NetSchoolResponse classAuditionList(@RequestParam Map<String, Object> params);

    /**
     * 获取课程埋点数据
     */
    @GetMapping(value = "/v4/common/class/class_sensors")
    NetSchoolResponse classSensors(@RequestParam("classId") int classIds);

    /**
     * fallback 处理
     */
    @Slf4j
    @Component
    class CourseServiceV5FallbackFactory implements Fallback<CourseServiceV5>{
        @Override
        public CourseServiceV5 create(Throwable throwable, HystrixCommand command) {
            return new CourseServiceV5(){
                /**
                 * 获取录播课程合集
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse recordClassList(Map<String, Object> params) {
                    log.error("course service v5 recordClassList fallback,params: {}, fall back reason: {} ",params, throwable);
                    return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
                }

                /**
                 * 获取直播课程合集
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse liveClassList(Map<String, Object> params) {
                    log.error("course service v5 liveClassList fallback,params: {}, fall back reason: {} ",params, throwable);
                    return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
                }

                /**
                 * 获取面库课程合集
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse icClassList(Map<String, Object> params) {
                    log.error("course service v5 icClassList fallback,params: {}, fall back reason: {} ",params, throwable);
                    return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
                }

                /**
                 * 课程播放接口
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getCommonUserPlay(Map<String, Object> params) {
                    log.error("course service v5 getCommonUserPlay fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 课程大纲-售前
                 * TODO: v3 Service 中有降级代码，但未被启用。
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse findTimetable(Map<String, Object> params) {
                    log.error("course service v5 findTimetable fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 课程大纲-售后
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse findPurchasesTimetable(Map<String, Object> params) {
                    log.error("course service v5 findPurchasesTimetable fallback,params: {}, fall back reason: {}",params, throwable);
                    /**
                     * 可能存在大批量请求数据的情况，比如pageSize = 1000
                     */
                    Map<String, Object> defaultMap = Maps.newLinkedHashMap();
                    defaultMap.put("list", Lists.newArrayList());
                    defaultMap.put("netClassName", "");
                    defaultMap.put("next", 0);
                    return new NetSchoolResponse(Result.SUCCESS_CODE, "", defaultMap);
                }

                /**
                 * 查询课程详情 - 录播
                 * 该接口在V5 之前有大量的业务处理。
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getClassDetailNotLive(Map<String, Object> params) {
                    log.error("course service v5 getClassDetailNotLive fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 查询课程详情 - 直播
                 * 该接口在V5 之前有大量的业务处理。
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getClassDetailLive(Map<String, Object> params) {
                    log.error("course service v5 getClassDetailLive fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取课程介绍
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getCourseIntroduction(Map<String, Object> params) {
                    log.error("course service v5 getCourseIntroduction fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取所有老师介绍
                 *
                 * @param teacherId
                 */
                @Override
                public NetSchoolResponse getCourseTeacherInfo(int teacherId) {
                    log.error("course service v5 getCourseTeacherInfo fallback,params: {}, fall back reason: {} ",teacherId, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取课程说明
                 * 获取 html 页面
                 *
                 * @param classId
                 * @param terminal
                 */
                @Override
                public String getClassExt(int classId, int terminal) {
                    log.error("course service v5 getClassExt fallback,params: {}, {}, fall back reason: {} ",classId, terminal, throwable);
                    return StringUtils.EMPTY;
                }

                /**
                 * 删除课程
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse deleteCourse(Map<String, Object> params) {
                    log.error("course service v5 deleteCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 彻底删除回收站课程
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse deleteDeepCourse(Map<String, Object> params) {
                    log.error("course service v5 deleteDeepCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 取消删除
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse cancelDeleteCourse(Map<String, Object> params) {
                    log.error("course service v5 cancelDeleteCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 置顶课程
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse postTopCourse(Map<String, Object> params) {
                    log.error("course service v5 postTopCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 删除置顶课程
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse deleteTopCourse(Map<String, Object> params) {
                    log.error("course service v5 deleteTopCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * qq群、课程学习总进度
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse qqGroupSchedule(Map<String, Object> params) {
                    log.error("course service v5 qqGroupSchedule fallback,params: {}, fall back reason: {}",params, throwable);
                    // todo 不报错处理
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取最后一次学习课程 - IC
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse lastStudyCourse(Map<String, Object> params) {
                    log.error("course service v5 lastStudyCourse fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 已够课程列表 - IC
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse userCourseList(Map<String, Object> params) {
                    log.error("course service v5 userCourseList fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 根据课程id 批量获取数据
                 *
                 * @param classIds
                 */
                @Override
                public NetSchoolResponse courseInfoList(String classIds) {
                    log.error("course service v5 courseInfoList fallback,params: {}, fall back reason: {}",classIds, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取横屏课程信息
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse chooseCourseWare(Map<String, Object> params) {
                    log.error("course service v5 chooseCourseWare fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 继续学习
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse lastPlayLesson(Map<String, Object> params) {
                    log.error("course service v5 lastPlayLesson fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 讲义
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse handouts(Map<String, Object> params) {
                    log.error("course service v5 handouts fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 课程详情活动促销
                 *
                 * @param classIds
                 */
                @Override
                public NetSchoolResponse appClassActivityDetails(int classIds, int terminal) {
                    log.error("course service v5 appClassActivityDetails fallback,params - classId: {}, terminal:{}, fall back reason: {}",classIds, terminal, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 试听列表
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse classAuditionList(Map<String, Object> params) {
                    log.error("course service v5 classAuditionList fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 获取课程埋点数据
                 *
                 * @param classIds
                 */
                @Override
                public NetSchoolResponse classSensors(int classIds) {
                    log.error("course service v5 classSensors fallback,params: {}, fall back reason: {}",classIds, throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }
}