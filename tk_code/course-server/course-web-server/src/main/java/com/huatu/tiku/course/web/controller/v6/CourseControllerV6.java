package com.huatu.tiku.course.web.controller.v6;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.CourseServiceV6;
import com.huatu.tiku.course.service.v6.CourseBizV6Service;
import com.huatu.tiku.course.service.v6.CourseServiceV6Biz;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 描述：课程接口v6
 *
 * @author biguodong
 * Create time 2018-11-26 下午4:22
 **/

@Slf4j
@RestController
@RequestMapping("/courses")
@ApiVersion("v6")
public class CourseControllerV6 {

    @Autowired
    private CourseServiceV6 courseService;

    @Autowired
    private CourseBizV6Service courseBizV6Service;

    @Autowired
    private CourseServiceV6Biz courseServiceV6Biz;

    /**
     * App课程列表
     *
     * @param cateId
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "list")
    public Object obtainCourseList(@RequestParam(value = "cateId") String cateId) {
        Map<String, Object> params = LocalMapParamHandler.get();
        return courseServiceV6Biz.obtainCourseList(params);
    }


    /**
     * 日历详情
     *
     * @param userSession
     * @param cv
     * @param date
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "calendarDetail")
    public Object obtainCalendarDetail(
            @Token UserSession userSession,
            @RequestHeader(value = "cv") String cv,
            @RequestHeader(value = "terminal") int terminal,
            @RequestParam(value = "date") String date,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Map<String, Object> params = LocalMapParamHandler.get();
        return courseBizV6Service.calendarDetail(params);
    }


    /**
     * 课程分类详情
     *
     * @param cateId
     * @param cv
     * @param terminal
     * @param page
     * @param typeId
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "typeDetail")
    public Object courseTypeDetail(@RequestHeader(value = "cv") String cv,
                                   @RequestHeader(value = "terminal") int terminal,
                                   @RequestParam(value = "cateId") int cateId,
                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "typeId") int typeId) {
        Map<String, Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = courseService.courseTypeDetail(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 课程搜索接口
     *
     * @param keyWord
     * @param page
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "search")
    public Object searchCourses(@Token(check = false, defaultValue = "") UserSession userSession,
                                @RequestParam(value = "keyWord") String keyWord,
                                @RequestParam(value = "cateId") int cateId,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "isHistory", defaultValue = "-1") int isHistory,
                                @RequestParam(value = "isRecommend", defaultValue = "-1") int isRecommend) {
        Map<String, Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = courseService.searchCourses(params);
        if (isHistory > 0) {
            courseServiceV6Biz.upSetSearchKeyWord(userSession.getToken(), keyWord);
        }
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 合集课程列表
     *
     * @param collectId
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "collectDetail")
    public Object collectDetail(@RequestParam(value = "collectId") long collectId,
                                @RequestParam(value = "page", defaultValue = "1") int page) {
        Map<String, Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = courseService.collectDetail(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 模考大赛解析课信息,多个id使用逗号分隔
     *
     * @param classIds
     * @return
     */
    @GetMapping(value = "courseAnalysis")
    public Object courseAnalysis(@RequestParam(value = "classIds") String classIds) {
        HashMap<String, LinkedHashMap> result = courseServiceV6Biz.getClassAnalysis(classIds);
        return result;
    }


    /**
     * 查询用户是否报名课程，课程是否免费，课程是否已结束
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @return
     */
    @GetMapping(value = "status/{classId}")
    public Object getUserCourseInfo(@Token UserSession userSession,
                                    @RequestHeader(value = "terminal") int terminal,
                                    @RequestHeader(value = "cv") String cv,
                                    @PathVariable int classId,
                                    @RequestParam(defaultValue = "-1") int collageActivityId) {
        return courseServiceV6Biz.getUserCourseStatus(userSession.getUname(), classId, collageActivityId);
    }

    /**
     * 小模考历史解析课信息列表
     *
     * @param
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "analysisClassList")
    public Object courseList(@RequestHeader int subject,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "size", defaultValue = "30") int size,
                             @RequestParam(value = "startTime", defaultValue = "-1") long startTime,
                             @RequestParam(value = "endTime", defaultValue = Long.MAX_VALUE + "") long endTime) {
        NetSchoolResponse netSchoolResponse = courseServiceV6Biz.analysisClassList(subject, page, size, startTime, endTime);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 添加秒杀课
     *
     * @param classId
     * @param limit
     * @return
     */
    @PostMapping(value = "/addSecKill")
    public Object addSecKillInfo(@RequestParam(value = "classId") String classId,
                                 @RequestParam(value = "limit") int limit) {
        courseServiceV6Biz.addSecKillInfo(classId, limit);
        return SuccessMessage.create("ok");
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{classId}/getClassDetail")
    public Object getClassDetail(
            @Token(check = false, defaultValue = "") UserSession userSession,
            @RequestHeader int terminal,
            @RequestHeader String cv,
            @PathVariable("classId") int classId,
            @RequestParam(defaultValue = "0") String collageActivityId
    ) {
        HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
                .put("classId", classId)
                .put("terminal", terminal)
                .put("userName", "uname")
                .put("collageActivityId", collageActivityId)
                .put("cv", cv)
                .build();
        log.warn("getClassDetailLive:{}, classId:{}, time:{}, cv:{}, terminal:{}", classId, String.valueOf(System.currentTimeMillis()), cv, terminal);
        return ResponseUtil.build(courseService.getClassDetail(map));
    }


    /**
     * 查询精准估分下直播课的状态和信息
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @return
     */
    @LogPrint
    @GetMapping(value = "baseInfo")
    public Object getClassInfoForEstimate(@Token(check = false) UserSession userSession,
                                          @RequestHeader(value = "terminal") int terminal,
                                          @RequestHeader(value = "cv") String cv,
                                          @RequestParam String classIds) {
        List<Integer> ids = Arrays.stream(classIds.split(",")).filter(NumberUtils::isDigits)
                .map(Integer::parseInt)
                .distinct()
                .collect(Collectors.toList());
        StopWatch stopwatch = new StopWatch("getClassInfoForEstimate");
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            ArrayList<Future<Map>> list = Lists.newArrayList();
            for (Integer classId : ids) {
                stopwatch.start("submit"+classIds);
                Future<Map> submit = executor.submit(() -> (Map) courseServiceV6Biz.getUserCourseStatus(
                        Optional.ofNullable(userSession).map(UserSession::getUname).orElse(Strings.EMPTY),
                        classId,
                        -1));
                list.add(submit);
                stopwatch.stop();
            }
            stopwatch.start("getBaseClassInfo");
            List<Map> classInfo = courseBizV6Service.getBaseClassInfo(ids);
            stopwatch.stop();
            if(CollectionUtils.isEmpty(classInfo)){
                return classInfo;
            }
            stopwatch.start("getClassInfo");
            List<Map> classStatus = getClassInfo(list);
            stopwatch.stop();
            stopwatch.start("convert");
            for (Map map : classInfo) {
                String id = MapUtils.getString(map, "id");
                map.put("isHaveLive",0);
                map.put("isBuy",0);
                map.put("actualPrice",0);
                classStatus.stream()
                        .filter(i->MapUtils.getString(i,"id", Strings.EMPTY).equalsIgnoreCase(id))
                        .findAny()
                        .ifPresent(i->{
                            map.putAll(i);
                        });
            }
            stopwatch.stop();
            return classInfo;

        }finally {
            if(stopwatch.isRunning()){
                stopwatch.stop();
            }
            log.info(stopwatch.prettyPrint());
        }

    }

    private List<Map> getClassInfo(ArrayList<Future<Map>> list) {
        return list.parallelStream()
                .map(future -> {
                    StopWatch stopWatch = new StopWatch("future");
                    stopWatch.start("1");
                    try {
                        return future.get(2, TimeUnit.SECONDS);
                    } catch (ExecutionException ee) { // 计算抛出一个异常
                        ee.printStackTrace();
                    } catch (InterruptedException ie) { // 当前线程在等待过程中被中断
                        ie.printStackTrace();
                    } catch (TimeoutException te) { // 在Future对象完成之前超过已过期
                        te.printStackTrace();
                    }finally {
                        if(stopWatch.isRunning()){
                            stopWatch.stop();
                        }
                        log.info(stopWatch.prettyPrint());
                    }
                    return Maps.newHashMap();
                })
                .collect(Collectors.toList());
    }
}
