package com.huatu.tiku.course.netschool.api.v3;

/**
 * @author hanchao
 * @date 2017/9/13 20:34
 */

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "course-service")
public interface CourseSettingServiceV3 {
    /**
     * 获取直播筛选条件
     * @return
     */
    @GetMapping("/v3/collectionClassSearch.php?do=list")
    NetSchoolResponse getLiveSettings();

    /**
     * 获取录播筛选条件
     * @return
     */
    @GetMapping("/v3/classSearch.php?do=list")
    NetSchoolResponse getRecordingSettings();

}
