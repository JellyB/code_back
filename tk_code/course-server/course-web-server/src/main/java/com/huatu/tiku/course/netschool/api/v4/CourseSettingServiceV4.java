package com.huatu.tiku.course.netschool.api.v4;

/**
 * @author hanchao
 * @date 2017/9/13 20:34
 */

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "course-service")
public interface CourseSettingServiceV4 {
    /**
     * 获取直播筛选条件
     * @return
     */
    @GetMapping("/v3/collectionClassSearch.php?do=cateList")
    NetSchoolResponse getLiveSettings(@RequestParam("userName") String userName);

    /**
     * 设置直播筛选条件
     * @return
     */
    @PostMapping("/v3/collectionClassSearch.php?do=set")
    NetSchoolResponse setLiveSettings(@RequestParam("userName") String userName,@RequestParam("setList") String categories);

    /**
     * 获取录播筛选条件
     * @return
     */
    @GetMapping("/v3/classSearch_new.php?do=cateList")
    NetSchoolResponse getRecordingSettings(@RequestParam("userName") String userName);


    /**
     * 设置录播筛选条件
     * @return
     */
    @GetMapping("/v3/classSearch_new.php?do=set")
    NetSchoolResponse setRecordingSettings(@RequestParam("userName") String userName,@RequestParam("setList") String categories);
}
