package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.SYDW_MY_LIST;
import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.SYDW_MY_LIVE_SUIT_DETAIL;
import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.SYDW_MY_LIVE_SUIT_LIST;
import static com.huatu.tiku.course.consts.NetSchoolUrlConst.*;

/**
 * 我的直播
 * @author hanchao
 * @date 2017/8/28 23:11
 */
@FeignClient(value = "course-service")
public interface UserCoursesServiceV1 {
    @RequestMapping(method = RequestMethod.GET,value=SYDW_MY_LIST)
    NetSchoolResponse mySydwList(@RequestParam Map<String,Object> params);

    @RequestMapping(method = RequestMethod.GET,value=MY_LIVE_SUIT_LIST)
    NetSchoolResponse myListNew(@RequestParam Map<String,Object> params);

    @RequestMapping(method = RequestMethod.GET,value=MY_LIST)
    NetSchoolResponse myList(@RequestParam Map<String,Object> params);

    @RequestMapping(method = RequestMethod.GET,value=HIDE_LIST)
    NetSchoolResponse myHideList(@RequestParam Map<String,Object> params);

    @RequestMapping(method = RequestMethod.GET,value=HIDE_COURSE)
    NetSchoolResponse hideCourse(@RequestParam Map<String,Object> params);

    @RequestMapping(method = RequestMethod.GET,value=SHOW_COURSE)
    NetSchoolResponse showCourse(@RequestParam Map<String,Object> params);


    @RequestMapping(method = RequestMethod.GET,value=MY_LIVE_SUIT_DETAIL)
    NetSchoolResponse myCourseDetail(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_MY_LIVE_SUIT_LIST)
    NetSchoolResponse mySydwListNew(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_MY_LIVE_SUIT_DETAIL)
    NetSchoolResponse mySydwCourseDetail(@RequestParam Map<String,Object> params);


    //----------------------------------

}
