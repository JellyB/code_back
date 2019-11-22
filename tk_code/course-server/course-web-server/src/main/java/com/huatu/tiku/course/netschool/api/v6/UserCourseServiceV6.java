package com.huatu.tiku.course.netschool.api.v6;

import java.util.Map;

import com.huatu.tiku.course.netschool.api.fall.UserCourseServiceV6FallBackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.PeriodTestListVO;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-26 下午5:28
 **/

@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = UserCourseServiceV6FallBackFactory.class)
public interface UserCourseServiceV6 {

    /**
     * 我的学习-日历接口
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/my_calendar")
    NetSchoolResponse obtainLearnCalender(@RequestParam Map<String, Object> params);


    /**
     * 我的-已过期课程
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/expired_class")
    NetSchoolResponse obtainExpiredCourses(@RequestParam Map<String, Object> params);


    /**
     * 我的课程等筛选列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/my_course_filter")
    NetSchoolResponse obtainCourseFilterList(@RequestParam Map<String, Object> params);

    /**
     * 我的课程
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/my_new_course")
    NetSchoolResponse obtainMineCourses(@RequestParam Map<String, Object> params);


    /**
     * 一键清除我的已过期课程
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/clear_expired")
    NetSchoolResponse clearExpiredCourses(@RequestParam Map<String, Object> params);

    /**
     * 课程所属考试接口
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/cate_list")
    NetSchoolResponse cateList(@RequestParam Map<String, Object> params);


    /**
     * 列表设置考试类型
     * @param params
     * @return
     */
    @PutMapping(value = "/v5/c/class/set_category")
    NetSchoolResponse setCategory(@RequestParam Map<String, Object> params);


    /**
     * 直播学习记录上报
     * @param params
     * @return
     */
    @PostMapping(value = "/v4/common/user/live_record")
    NetSchoolResponse saveLiveRecord(@RequestParam Map<String, Object> params);
    
	/**
	 * 获取未完成的阶段测试列表
	 * 
	 * @param params
	 * @return
	 */
	@GetMapping(value = "/v5/c/class/unfinish_stage_exam_list")
	NetSchoolResponse<PeriodTestListVO> unfinishStageExamList(@RequestParam Map<String, Object> params);
	
	/**
	 * 阶段测完成试状态上报php
	 * @param params
	 * @return
	 */
	@PostMapping(value = "/v5/c/class/stage_test_study_record")
	NetSchoolResponse stageTestStudyRecord(@RequestParam Map<String, Object> params);
	
	/**
	 * 用户未完成阶段测试总数
	 * @param params
	 * @return
	 */
	@GetMapping(value = "/v5/c/class/unfinish_exam_num")
	NetSchoolResponse unfinishStageExamCount(@RequestParam Map<String, String> params);


	/**
	 * 阶段测试单条获取全部已读
	 * @param params
	 * @return
	 */
	@PostMapping(value = "/v5/c/class/unfinish_exam_status_record")
	NetSchoolResponse readPeriod(@RequestParam Map<String, Object> params);

	/**
	 * 一对一信息提交
	 * @param p
	 * @return
	 */
	@PostMapping(value = "/v5/c/user/one_to_one")
	NetSchoolResponse one2One(@RequestParam(value = "p") String p);


	/**
	 * 一对一信息获取
	 * @param p
	 * @return
	 */
	@GetMapping(value = "/v5/c/user/one_to_one")
	NetSchoolResponse obtainOne2One(@RequestParam(value = "p") String p, @RequestParam(value = "terminal") int terminal);

}
