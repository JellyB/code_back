package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.UserCourseServiceV3Fallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/13 16:37
 */
@FeignClient(value = "course-service",fallback = UserCourseServiceV3Fallback.class)
public interface UserCoursesServiceV3 {
    /**
     * 获取用户购买的所有课程id集合
     * @return
     */
    @PostMapping("/v3/classesIsBuy.php")
    NetSchoolResponse findProducts(@RequestParam Map<String,Object> params);


    /**
     * 获取用户购买的所有课程id集合
     * @return
     */
    @PostMapping("/v3/checkIsBuyByUserName.php")
    NetSchoolResponse getProductIsBuy(@RequestParam Map<String,Object> params);
    /**
     * 我的课程列表
     * @param params
     * @return
     */
    @PostMapping("/v3/myCourse_new.php")
    NetSchoolResponse findUserCourses(@RequestParam Map<String,Object> params);

    /**
     * 我的隐藏的课表列表
     * @param params
     * @return
     */
    @PostMapping("/v3/myHideClasses.php")
    NetSchoolResponse findUserHideCourses(@RequestParam Map<String,Object> params);

    /**
     * 隐藏课程
     * @param params
     * @return
     */
    @PostMapping("/v3/delClass.php")
    NetSchoolResponse hideCourse(@RequestParam Map<String,Object> params);


    @PostMapping("/v3/recoverClass.php")
    NetSchoolResponse showCourse(@RequestParam Map<String,Object> params);


    /**
     * 我的课程（套餐课）的主页接口
     * @param params
     * @return
     */
    @RequestMapping(method = RequestMethod.GET,value="/v3/packageClassList.php")
    NetSchoolResponse getMyPackCourseDetail(@RequestParam Map<String,Object> params);


    /**
     * 我的课程日历
     * @param username
     * @return
     */
    @GetMapping("/v3/liveCalendar.php?action=hasLive")
    NetSchoolResponse getLiveCalendar(@RequestParam("username") String username);


    /**
     * 日历直播详情
     * @param ids
     * @return
     */
    @GetMapping("/v3/liveCalendar.php?action=calendarDetail")
    NetSchoolResponse getCalendarDetail(@RequestParam("id") String ids);

    /**
     * 填写1v1报名表
     * @return
     */
    @PostMapping(value="/v3/oneToOne.php",consumes = MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE)
    NetSchoolResponse save1V1Table(Map<String,Object> p);

    /**
     * 获取1v1报名表
     */
    @GetMapping("/v3/oneToOne.php")
    NetSchoolResponse get1V1Table(@RequestParam("p")String p);

    /**
     * 获取高端面试课信息
     */
    @GetMapping("/v3/order/gdmsinfo.php")
    NetSchoolResponse getHighEndInfo(@RequestParam("p")String p);

    /**
     * b奥村高端面试课信息
     */
    @PostMapping(value = "/v3/order/gdmsinfo.php",consumes = MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE)
    NetSchoolResponse setHighEndInfo(Map<String,Object> params);

    /**
     * 高端面试课赠送
     */
    @GetMapping(value = "/v3/gdmsZengsong.php")
    NetSchoolResponse highendRecieve(@RequestParam("p")String p);
}
