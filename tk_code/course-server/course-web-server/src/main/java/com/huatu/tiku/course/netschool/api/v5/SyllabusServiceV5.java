package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lijun on 2018/11/7
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/")
public interface SyllabusServiceV5 {

    /**
     * 大纲 售后
     */
    @GetMapping(value = "/class/buy_after_syllabus")
    NetSchoolResponse buyAfterSyllabus(@RequestParam Map<String, Object> params);

    /**
     * 售后大纲课程列表（7.1.1）
     */
    @GetMapping(value = "/class/syllabus_classes")
    NetSchoolResponse syllabusClasses(@RequestParam Map<String, Object> params);

    /**
     * 售后大纲老师列表（7.1.1）
     */
    @GetMapping(value = "/class/syllabus_teachers")
    NetSchoolResponse syllabusTeachers(@RequestParam Map<String, Object> params);
}
