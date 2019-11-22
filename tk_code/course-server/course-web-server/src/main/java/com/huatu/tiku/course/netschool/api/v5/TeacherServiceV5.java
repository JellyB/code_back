package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lijun on 2018/6/26
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/teacher")
public interface TeacherServiceV5 {

    /**
     * 老师在售课程更多
     */
    @GetMapping(value = "/pay_classes_all")
    NetSchoolResponse getPayClassesAll(@RequestParam Map<String, Object> map);

    /**
     * 获取老师详情
     */
    @GetMapping(value = "/teacher_details")
    NetSchoolResponse getTeacherDetail(@RequestParam("teacherId") int teacherId);

    /**
     * 老师详情页 历史课件列表
     */
    @GetMapping(value = "/history_lesson_list")
    NetSchoolResponse historyLessonList(@RequestParam Map<String, Object> params);

    /***
     * 老师详情页评价历史课程列表
     */
    @GetMapping(value = "/history_course")
    NetSchoolResponse historyCourse(@RequestParam Map<String, Object> params);

    /**
     * 老师详情页评价课件评价列表
     */
    @GetMapping(value = "lesson_evaluate_list")
    NetSchoolResponse lessonEvaluateList(@RequestParam Map<String, Object> params);
}
