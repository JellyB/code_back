package com.huatu.tiku.course.netschool.api.v6;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV6FallBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-26 下午3:22
 **/

@FeignClient(value = "o-course-service", path = "/lumenapi",fallback = CourseServiceV6FallBack.class)
public interface CourseServiceV6 {


    /**
     * app课程列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/app_list")
    NetSchoolResponse obtainCourseList(@RequestParam Map<String, Object> params);


    /**
     * 日历详情接口
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/live_detail")
    NetSchoolResponse calendarDetail(@RequestParam Map<String, Object> params);

    /**
     * 课程分类详情
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/type_detail")
    NetSchoolResponse courseTypeDetail(@RequestParam Map<String,Object> params);

    /**
     * 课程搜索接口
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/search")
    NetSchoolResponse searchCourses(@RequestParam Map<String, Object> params);


    /**
     * 合集课程列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/collect_detail")
    NetSchoolResponse collectDetail(@RequestParam Map<String, Object> params);

    /**
     * 获取解析课课程信息 pc 端模考大赛专用
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/analysis")
    NetSchoolResponse analysis(@RequestParam Map<String,Object> params);

    /**
     * 小模考课程列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/analysis_class_list")
    NetSchoolResponse analysisClassList(@RequestParam Map<String, Object> params);


    /**
     * 查询用户是否报名课程，课程是否免费，课程是否已结束
     * @param params
     * @return
     */
    @GetMapping(value = "v5/c/class/is_live_in_class")
    NetSchoolResponse userCourseStatus(@RequestParam Map<String, Object> params);


    /**
     * 查询课程详情
     */
    @GetMapping(value = "/v4/common/class/class_detail")
    NetSchoolResponse getClassDetail(@RequestParam Map<String, Object> params);


    @GetMapping(value = "v5/c/class/share_class_details")
    NetSchoolResponse getBaseClassInfo(@RequestParam Map<String, Object> params);
}
