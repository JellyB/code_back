package com.huatu.tiku.course.netschool.api.v4;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV4Fallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2018/3/6 14:46
 */
@FeignClient(value = "course-service",fallback = CourseServiceV4Fallback.class)
public interface CourseServiceV4 {
    /**
     * 录播课程列表
     * @param params
     * @return
     */
    @GetMapping("/v3/classSearch_new.php")
    NetSchoolResponse findRecordingList(@RequestParam Map<String,Object> params);
}
